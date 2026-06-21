package com.example.perfsdk

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.perfsdk.config.PerfSDKConfig
import com.example.perfsdk.device.DeviceInfoCollector
import com.example.perfsdk.lifecycle.PerfLifecycleObserver
import com.example.perfsdk.model.EventType
import com.example.perfsdk.model.PerformanceEvent
import com.example.perfsdk.network.BatchSender
import com.example.perfsdk.network.PerfInterceptor
import com.example.perfsdk.network.RetrofitProvider
import okhttp3.Interceptor
import com.example.perfsdk.storage.EventRepository
import com.example.perfsdk.storage.PerfDatabase
import com.example.perfsdk.worker.EventUploadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

object PerfSDK {

    private const val TAG = "PerfSDK"
    private const val BASE_URL = "http://10.0.2.2:8000/"
    private const val FLUSH_INTERVAL_MS = 30_000L

    val sessionId: String = UUID.randomUUID().toString()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeTraces = ConcurrentHashMap<String, Long>()
    private val activeScreenTraces = ConcurrentHashMap<String, Long>()
    private val fallbackQueue = CopyOnWriteArrayList<PerformanceEvent>()

    @Volatile private var config: PerfSDKConfig? = null
    @Volatile private var repository: EventRepository? = null
    @Volatile private var batchSender: BatchSender? = null
    @Volatile private var deviceCollector: DeviceInfoCollector? = null
    @Volatile private var appVersion: String = ""

    fun init(context: Context, apiKey: String) {
        if (config != null) {
            Log.w(TAG, "PerfSDK already initialized")
            return
        }

        val initTime = System.currentTimeMillis()
        val appContext = context.applicationContext

        appVersion = try {
            appContext.packageManager
                .getPackageInfo(appContext.packageName, 0)
                .versionName ?: ""
        } catch (e: Exception) { "" }

        config = PerfSDKConfig(appContext, apiKey)
        deviceCollector = DeviceInfoCollector(appContext)

        appContext.getSharedPreferences(EventUploadWorker.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(EventUploadWorker.KEY_API_KEY, apiKey)
            .putString(EventUploadWorker.KEY_BASE_URL, BASE_URL)
            .apply()

        val db = PerfDatabase.getInstance(appContext)
        val repo = EventRepository(db.eventDao())
        repository = repo

        val api = RetrofitProvider.create(BASE_URL)
        batchSender = BatchSender(repo, api, apiKey)

        Log.d(TAG, "PerfSDK initialized")

        val pending = fallbackQueue.toList()
        if (pending.isNotEmpty()) {
            fallbackQueue.clear()
            scope.launch { pending.forEach { repo.saveEvent(it) } }
        }

        schedulePeriodicWorker(appContext)
        startPeriodicFlush()

        val observer = PerfLifecycleObserver(initTime) { type, name, start, end ->
            saveEventAsync(buildEvent(type, name, start, end))
        }
        (appContext as? Application)?.registerActivityLifecycleCallbacks(observer)
        Log.d(TAG, "Lifecycle observer registered")
    }

    // ── Custom trace ──────────────────────────────────────────────────────────

    fun startTrace(traceName: String) {
        if (traceName.isBlank()) {
            Log.e(TAG, "Trace name cannot be empty")
            return
        }
        if (activeTraces.containsKey(traceName)) {
            Log.w(TAG, "Trace already started: $traceName")
            return
        }
        activeTraces[traceName] = System.currentTimeMillis()
        Log.d(TAG, "Trace started: $traceName")
    }

    fun stopTrace(traceName: String) {
        val startTime = activeTraces.remove(traceName)
        if (startTime == null) {
            Log.e(TAG, "Trace was not started: $traceName")
            return
        }
        val endTime = System.currentTimeMillis()
        val event = buildEvent(
            eventType = EventType.CUSTOM_TRACE,
            eventName = traceName,
            startTime = startTime,
            endTime = endTime
        )
        Log.d(TAG, "Trace finished: $traceName, duration: ${event.durationMs} ms")
        saveEventAsync(event)
    }

    // ── Screen trace ──────────────────────────────────────────────────────────

    fun startScreenTrace(screenName: String) {
        if (screenName.isBlank()) {
            Log.e(TAG, "Screen name cannot be empty")
            return
        }
        if (activeScreenTraces.containsKey(screenName)) {
            Log.w(TAG, "Screen trace already started: $screenName")
            return
        }
        activeScreenTraces[screenName] = System.currentTimeMillis()
        Log.d(TAG, "Screen trace started: $screenName")
    }

    fun stopScreenTrace(screenName: String) {
        val startTime = activeScreenTraces.remove(screenName)
        if (startTime == null) {
            Log.e(TAG, "Screen trace was not started: $screenName")
            return
        }
        val endTime = System.currentTimeMillis()
        val event = buildEvent(
            eventType = EventType.SCREEN_LOAD,
            eventName = screenName,
            startTime = startTime,
            endTime = endTime
        )
        Log.d(TAG, "Screen trace finished: $screenName, duration: ${event.durationMs} ms")
        saveEventAsync(event)
    }

    // ── Network call tracking ─────────────────────────────────────────────────

    fun trackNetworkCall(method: String, endpoint: String, statusCode: Int, latencyMs: Long) {
        val endTime = System.currentTimeMillis()
        val device = deviceCollector?.collect()
        val event = PerformanceEvent(
            eventId = UUID.randomUUID().toString(),
            sessionId = sessionId,
            eventType = EventType.NETWORK_REQUEST,
            eventName = "$method $endpoint",
            startTime = endTime - latencyMs,
            endTime = endTime,
            durationMs = latencyMs,
            timestamp = endTime,
            appVersion = appVersion,
            androidVersion = Build.VERSION.RELEASE,
            deviceModel = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            networkType = device?.networkType ?: "UNKNOWN",
            networkSubtype = device?.networkSubtype ?: "",
            availableRamMb = device?.availableRamMb ?: 0L,
            isBatterySaverActive = device?.isBatterySaverActive ?: false,
            apiLevel = device?.apiLevel ?: Build.VERSION.SDK_INT,
            isEmulator = device?.isEmulator ?: false,
            appPackageName = device?.appPackageName ?: "",
            endpoint = endpoint,
            method = method,
            statusCode = statusCode,
            success = statusCode in 200..299
        )
        Log.d(TAG, "Network call tracked: $method $endpoint $statusCode (${latencyMs}ms)")
        saveEventAsync(event)
    }

    // ── OkHttp interceptor ───────────────────────────────────────────────────

    fun okHttpInterceptor(): Interceptor = PerfInterceptor()

    // ── Flush ─────────────────────────────────────────────────────────────────

    fun flush() {
        scope.launch { batchSender?.sendPendingBatch() }

        val ctx = config?.context ?: return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val oneTime = OneTimeWorkRequestBuilder<EventUploadWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(ctx).enqueue(oneTime)
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun buildEvent(
        eventType: EventType,
        eventName: String,
        startTime: Long,
        endTime: Long
    ): PerformanceEvent {
        val device = deviceCollector?.collect()
        return PerformanceEvent(
            eventId = UUID.randomUUID().toString(),
            sessionId = sessionId,
            eventType = eventType,
            eventName = eventName,
            startTime = startTime,
            endTime = endTime,
            durationMs = endTime - startTime,
            timestamp = endTime,
            appVersion = appVersion,
            androidVersion = Build.VERSION.RELEASE,
            deviceModel = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            networkType = device?.networkType ?: "UNKNOWN",
            networkSubtype = device?.networkSubtype ?: "",
            availableRamMb = device?.availableRamMb ?: 0L,
            isBatterySaverActive = device?.isBatterySaverActive ?: false,
            apiLevel = device?.apiLevel ?: Build.VERSION.SDK_INT,
            isEmulator = device?.isEmulator ?: false,
            appPackageName = device?.appPackageName ?: ""
        )
    }

    private fun saveEventAsync(event: PerformanceEvent) {
        val repo = repository
        if (repo != null) {
            scope.launch {
                repo.saveEvent(event)
                if (repo.countPendingEvents() >= BatchSender.BATCH_SIZE) {
                    batchSender?.sendPendingBatch()
                }
            }
        } else {
            Log.w(TAG, "PerfSDK not initialized — event queued in memory")
            fallbackQueue.add(event)
        }
    }

    private fun schedulePeriodicWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<EventUploadWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EventUploadWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )

        Log.d(TAG, "WorkManager upload job scheduled")
    }

    private fun startPeriodicFlush() {
        scope.launch {
            while (true) {
                delay(FLUSH_INTERVAL_MS)
                batchSender?.sendPendingBatch()
            }
        }
    }
}

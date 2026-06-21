package com.example.perfsdk.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.perfsdk.network.BatchSender
import com.example.perfsdk.network.RetrofitProvider
import com.example.perfsdk.storage.EventRepository
import com.example.perfsdk.storage.PerfDatabase

internal class EventUploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val apiKey = prefs.getString(KEY_API_KEY, null)
            ?: return Result.failure()   // SDK never initialized — no point retrying
        val baseUrl = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL

        val db = PerfDatabase.getInstance(applicationContext)
        val repo = EventRepository(db.eventDao())
        val api = RetrofitProvider.create(baseUrl)
        val sender = BatchSender(repo, api, apiKey)

        val success = sender.sendPendingBatch()
        return if (success) Result.success() else Result.retry()
    }

    companion object {
        const val PREFS_NAME = "perfsdk_prefs"
        const val KEY_API_KEY = "api_key"
        const val KEY_BASE_URL = "base_url"
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"
        const val WORK_NAME = "perf_sdk_upload"
    }
}

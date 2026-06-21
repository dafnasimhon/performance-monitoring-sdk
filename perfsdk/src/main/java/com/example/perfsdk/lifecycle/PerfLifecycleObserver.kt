package com.example.perfsdk.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.perfsdk.model.EventType
import java.util.concurrent.ConcurrentHashMap

internal class PerfLifecycleObserver(
    private val appStartTime: Long,
    private val onEvent: (type: EventType, name: String, startTime: Long, endTime: Long) -> Unit
) : Application.ActivityLifecycleCallbacks {

    private val activityCreateTimes = ConcurrentHashMap<String, Long>()
    @Volatile private var appStartupRecorded = false

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activityCreateTimes[key(activity)] = System.currentTimeMillis()
    }

    override fun onActivityResumed(activity: Activity) {
        val endTime = System.currentTimeMillis()

        if (!appStartupRecorded) {
            appStartupRecorded = true
            onEvent(EventType.APP_STARTUP, "AppStartup", appStartTime, endTime)
        }

        val createTime = activityCreateTimes.remove(key(activity)) ?: return
        onEvent(EventType.SCREEN_LOAD, activity.javaClass.simpleName, createTime, endTime)
    }

    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) {
        activityCreateTimes.remove(key(activity))
    }

    private fun key(activity: Activity) = System.identityHashCode(activity).toString()
}

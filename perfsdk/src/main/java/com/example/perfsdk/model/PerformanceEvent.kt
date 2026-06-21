package com.example.perfsdk.model

import android.os.Build

data class PerformanceEvent(
    val eventId: String,
    val sessionId: String,
    val eventType: EventType,
    val eventName: String,
    val startTime: Long?,
    val endTime: Long?,
    val durationMs: Long,
    val timestamp: Long,
    val appVersion: String,
    val androidVersion: String,
    val deviceModel: String,
    val manufacturer: String,
    val networkType: String,
    val networkSubtype: String = "",
    val availableRamMb: Long = 0L,
    val isBatterySaverActive: Boolean = false,
    val apiLevel: Int = Build.VERSION.SDK_INT,
    val isEmulator: Boolean = false,
    val appPackageName: String = "",
    val endpoint: String? = null,
    val method: String? = null,
    val statusCode: Int? = null,
    val success: Boolean? = null
)

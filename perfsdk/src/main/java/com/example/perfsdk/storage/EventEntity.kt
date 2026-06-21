package com.example.perfsdk.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
internal data class EventEntity(
    @PrimaryKey val eventId: String,
    val sessionId: String,
    val eventType: String,
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
    val apiLevel: Int = 0,
    val isEmulator: Boolean = false,
    val appPackageName: String = "",
    val endpoint: String?,
    val method: String?,
    val statusCode: Int?,
    val success: Boolean?,
    val uploadStatus: String = UploadStatus.PENDING.name
)

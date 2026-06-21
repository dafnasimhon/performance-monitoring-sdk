package com.example.perfsdk.storage

import com.example.perfsdk.model.EventType
import com.example.perfsdk.model.PerformanceEvent

internal class EventRepository(private val dao: EventDao) {

    suspend fun saveEvent(event: PerformanceEvent) {
        dao.insertEvent(event.toEntity())
    }

    suspend fun getPendingEvents(limit: Int = 20): List<PerformanceEvent> {
        return dao.getPendingEvents(limit).map { it.toPerformanceEvent() }
    }

    suspend fun markEventsAsSending(ids: List<String>) = dao.markEventsAsSending(ids)

    suspend fun markEventsAsFailed(ids: List<String>) = dao.markEventsAsFailed(ids)

    suspend fun deleteEvents(ids: List<String>) = dao.deleteEvents(ids)

    suspend fun resetStuckEvents() = dao.resetStuckEvents()

    suspend fun countPendingEvents(): Int = dao.countPendingEvents()
}

private fun PerformanceEvent.toEntity() = EventEntity(
    eventId = eventId,
    sessionId = sessionId,
    eventType = eventType.name,
    eventName = eventName,
    startTime = startTime,
    endTime = endTime,
    durationMs = durationMs,
    timestamp = timestamp,
    appVersion = appVersion,
    androidVersion = androidVersion,
    deviceModel = deviceModel,
    manufacturer = manufacturer,
    networkType = networkType,
    networkSubtype = networkSubtype,
    availableRamMb = availableRamMb,
    isBatterySaverActive = isBatterySaverActive,
    apiLevel = apiLevel,
    isEmulator = isEmulator,
    appPackageName = appPackageName,
    endpoint = endpoint,
    method = method,
    statusCode = statusCode,
    success = success,
    uploadStatus = UploadStatus.PENDING.name
)

private fun EventEntity.toPerformanceEvent() = PerformanceEvent(
    eventId = eventId,
    sessionId = sessionId,
    eventType = EventType.valueOf(eventType),
    eventName = eventName,
    startTime = startTime,
    endTime = endTime,
    durationMs = durationMs,
    timestamp = timestamp,
    appVersion = appVersion,
    androidVersion = androidVersion,
    deviceModel = deviceModel,
    manufacturer = manufacturer,
    networkType = networkType,
    networkSubtype = networkSubtype,
    availableRamMb = availableRamMb,
    isBatterySaverActive = isBatterySaverActive,
    apiLevel = apiLevel,
    isEmulator = isEmulator,
    appPackageName = appPackageName,
    endpoint = endpoint,
    method = method,
    statusCode = statusCode,
    success = success
)

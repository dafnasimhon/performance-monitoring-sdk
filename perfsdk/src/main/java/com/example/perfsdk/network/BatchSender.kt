package com.example.perfsdk.network

import android.util.Log
import com.example.perfsdk.storage.EventRepository

internal class BatchSender(
    private val repository: EventRepository,
    private val apiService: PerfApiService,
    private val apiKey: String
) {
    companion object {
        private const val TAG = "PerfSDK"
        const val BATCH_SIZE = 20
    }

    suspend fun sendPendingBatch(): Boolean {
        repository.resetStuckEvents()
        val events = repository.getPendingEvents(limit = BATCH_SIZE)
        if (events.isEmpty()) return true

        val ids = events.map { it.eventId }
        repository.markEventsAsSending(ids)

        return try {
            val response = apiService.sendBatch(
                apiKey = apiKey,
                request = BatchRequest(
                    sentAt = System.currentTimeMillis(),
                    events = events
                )
            )
            repository.deleteEvents(ids)
            Log.d(TAG, "Batch sent: ${response.accepted} events accepted")
            true
        } catch (e: Exception) {
            repository.markEventsAsFailed(ids)
            Log.e(TAG, "Batch send failed: ${e.message}")
            false
        }
    }
}

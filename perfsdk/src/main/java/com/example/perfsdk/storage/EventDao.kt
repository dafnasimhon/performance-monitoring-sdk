package com.example.perfsdk.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface EventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: EventEntity)

    @Query("SELECT * FROM events WHERE uploadStatus = 'PENDING' LIMIT :limit")
    suspend fun getPendingEvents(limit: Int): List<EventEntity>

    @Query("UPDATE events SET uploadStatus = 'SENDING' WHERE eventId IN (:ids)")
    suspend fun markEventsAsSending(ids: List<String>)

    @Query("UPDATE events SET uploadStatus = 'FAILED' WHERE eventId IN (:ids)")
    suspend fun markEventsAsFailed(ids: List<String>)

    @Query("DELETE FROM events WHERE eventId IN (:ids)")
    suspend fun deleteEvents(ids: List<String>)

    @Query("UPDATE events SET uploadStatus = 'PENDING' WHERE uploadStatus IN ('FAILED', 'SENDING')")
    suspend fun resetStuckEvents()

    @Query("SELECT COUNT(*) FROM events WHERE uploadStatus = 'PENDING'")
    suspend fun countPendingEvents(): Int

    @Query("SELECT COUNT(*) FROM events")
    suspend fun countAllEvents(): Int

    @Query("DELETE FROM events WHERE eventId IN (SELECT eventId FROM events ORDER BY timestamp ASC LIMIT :count)")
    suspend fun deleteOldestEvents(count: Int)
}

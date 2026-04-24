package com.erivaldogelson.remedios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erivaldogelson.remedios.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY triggerAt ASC")
    fun observeReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isActive = 1 ORDER BY triggerAt ASC LIMIT 1")
    fun observeActiveReminder(): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders ORDER BY triggerAt ASC")
    suspend fun getAll(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<ReminderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: ReminderEntity): Long

    @Query("DELETE FROM reminders")
    suspend fun clearAll()

    @Query("UPDATE reminders SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateActiveReminders()

    @Query(
        """
        UPDATE reminders
        SET isActive = 1
        WHERE medicationId = :medicationId
          AND ((:scheduleId IS NULL AND scheduleId IS NULL) OR scheduleId = :scheduleId)
          AND triggerAt = :triggerAt
        """,
    )
    suspend fun activateReminder(medicationId: Long, scheduleId: Long?, triggerAt: java.time.LocalDateTime)

    @Query(
        """
        UPDATE reminders
        SET isActive = 0
        WHERE medicationId = :medicationId
          AND ((:scheduleId IS NULL AND scheduleId IS NULL) OR scheduleId = :scheduleId)
          AND triggerAt = :triggerAt
        """,
    )
    suspend fun deactivateReminder(medicationId: Long, scheduleId: Long?, triggerAt: java.time.LocalDateTime)
}

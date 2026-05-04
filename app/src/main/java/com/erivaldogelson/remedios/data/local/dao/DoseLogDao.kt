package com.erivaldogelson.remedios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erivaldogelson.remedios.data.local.entity.DoseLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_logs ORDER BY scheduledAt DESC")
    fun observeLogs(): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE medicationId = :medicationId ORDER BY scheduledAt DESC")
    suspend fun getLogsForMedication(medicationId: Long): List<DoseLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(entity: DoseLogEntity): Long
}

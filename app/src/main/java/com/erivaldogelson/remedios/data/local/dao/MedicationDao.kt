package com.erivaldogelson.remedios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.erivaldogelson.remedios.data.local.entity.DoseScheduleEntity
import com.erivaldogelson.remedios.data.local.entity.MedicationEntity
import com.erivaldogelson.remedios.data.local.entity.MedicationImageEntity
import com.erivaldogelson.remedios.data.local.relation.MedicationWithAssets
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Transaction
    @Query("SELECT * FROM medications ORDER BY updatedAt DESC")
    fun observeMedicationCards(): Flow<List<MedicationWithAssets>>

    @Transaction
    @Query("SELECT * FROM medications ORDER BY updatedAt DESC")
    suspend fun getMedicationCards(): List<MedicationWithAssets>

    @Transaction
    @Query("SELECT * FROM medications WHERE id = :id")
    fun observeMedication(id: Long): Flow<MedicationWithAssets?>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Long): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(entity: MedicationEntity): Long

    @Update
    suspend fun updateMedication(entity: MedicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<DoseScheduleEntity>)

    @Query("DELETE FROM dose_schedules WHERE medicationId = :medicationId")
    suspend fun deleteSchedulesForMedication(medicationId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<MedicationImageEntity>)

    @Query("DELETE FROM medication_images WHERE medicationId = :medicationId")
    suspend fun deleteImagesForMedication(medicationId: Long)

    @Query("SELECT COUNT(*) FROM medications")
    suspend fun count(): Int
}

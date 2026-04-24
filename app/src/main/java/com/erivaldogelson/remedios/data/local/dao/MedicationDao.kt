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

    @Query("DELETE FROM medications WHERE id = :medicationId")
    suspend fun deleteMedicationById(medicationId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<DoseScheduleEntity>)

    @Query("DELETE FROM dose_schedules WHERE medicationId = :medicationId")
    suspend fun deleteSchedulesForMedication(medicationId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<MedicationImageEntity>)

    @Query("DELETE FROM medication_images WHERE medicationId = :medicationId")
    suspend fun deleteImagesForMedication(medicationId: Long)

    @Query("DELETE FROM dose_logs WHERE medicationId = :medicationId")
    suspend fun deleteDoseLogsForMedication(medicationId: Long)

    @Query("DELETE FROM reminders WHERE medicationId = :medicationId")
    suspend fun deleteRemindersForMedication(medicationId: Long)

    @Transaction
    suspend fun deleteMedicationWithData(medicationId: Long) {
        deleteDoseLogsForMedication(medicationId)
        deleteRemindersForMedication(medicationId)
        deleteSchedulesForMedication(medicationId)
        deleteImagesForMedication(medicationId)
        deleteMedicationById(medicationId)
    }

    @Query("SELECT COUNT(*) FROM medications")
    suspend fun count(): Int

    @Transaction
    suspend fun deleteBundledSampleMedicationData() {
        deleteSampleDoseLogs()
        deleteSampleReminders()
        deleteSampleSchedules()
        deleteSampleImages()
        deleteSampleMedications()
    }

    @Query(
        """
        DELETE FROM dose_logs
        WHERE medicationId IN (
            SELECT id FROM medications
            WHERE (name = 'Vitamina D3' AND manufacturer = 'NutriLab')
               OR (name = 'Ibuprofeno' AND manufacturer = 'Saúde Farma')
        )
        """,
    )
    suspend fun deleteSampleDoseLogs()

    @Query(
        """
        DELETE FROM reminders
        WHERE medicationId IN (
            SELECT id FROM medications
            WHERE (name = 'Vitamina D3' AND manufacturer = 'NutriLab')
               OR (name = 'Ibuprofeno' AND manufacturer = 'Saúde Farma')
        )
        """,
    )
    suspend fun deleteSampleReminders()

    @Query(
        """
        DELETE FROM dose_schedules
        WHERE medicationId IN (
            SELECT id FROM medications
            WHERE (name = 'Vitamina D3' AND manufacturer = 'NutriLab')
               OR (name = 'Ibuprofeno' AND manufacturer = 'Saúde Farma')
        )
        """,
    )
    suspend fun deleteSampleSchedules()

    @Query(
        """
        DELETE FROM medication_images
        WHERE medicationId IN (
            SELECT id FROM medications
            WHERE (name = 'Vitamina D3' AND manufacturer = 'NutriLab')
               OR (name = 'Ibuprofeno' AND manufacturer = 'Saúde Farma')
        )
        """,
    )
    suspend fun deleteSampleImages()

    @Query(
        """
        DELETE FROM medications
        WHERE (name = 'Vitamina D3' AND manufacturer = 'NutriLab')
           OR (name = 'Ibuprofeno' AND manufacturer = 'Saúde Farma')
        """,
    )
    suspend fun deleteSampleMedications()
}

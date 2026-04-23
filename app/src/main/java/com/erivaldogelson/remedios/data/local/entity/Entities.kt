package com.erivaldogelson.remedios.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.erivaldogelson.remedios.domain.model.DoseStatus
import com.erivaldogelson.remedios.domain.model.ImageSource
import com.erivaldogelson.remedios.domain.model.MedicationForm
import com.erivaldogelson.remedios.domain.model.ReminderAction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val dosage: String,
    val form: MedicationForm,
    val frequencyLabel: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val instructions: String,
    val notes: String,
    val quantityRemaining: Int,
    val accentColor: Long,
    val iconEmoji: String,
    val manufacturer: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

@Entity(tableName = "dose_schedules")
data class DoseScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val medicationId: Long,
    val time: LocalTime,
    val reminderOffsetMinutes: Int = 0,
    val isEnabled: Boolean = true,
)

@Entity(tableName = "dose_logs")
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val medicationId: Long,
    val scheduleId: Long?,
    val scheduledAt: LocalDateTime,
    val actualAt: LocalDateTime?,
    val status: DoseStatus,
    val note: String = "",
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val medicationId: Long,
    val scheduleId: Long?,
    val triggerAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val isActive: Boolean,
    val action: ReminderAction? = null,
)

@Entity(tableName = "medication_images")
data class MedicationImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val medicationId: Long,
    val uri: String,
    val source: ImageSource,
    val ocrRawText: String = "",
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 0,
    val themeMode: String,
    val dynamicColorEnabled: Boolean,
    val onboardingCompleted: Boolean,
    val liveUpdatesEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val languageTag: String,
    val updatedAt: LocalDateTime,
)


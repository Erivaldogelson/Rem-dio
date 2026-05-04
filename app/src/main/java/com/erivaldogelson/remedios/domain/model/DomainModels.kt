package com.erivaldogelson.remedios.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class MedicationForm {
    TABLET,
    CAPSULE,
    SYRUP,
    INJECTION,
    DROPS,
    CREAM,
    OTHER,
}

enum class DoseStatus {
    TAKEN,
    SNOOZED,
    SKIPPED,
    MISSED,
    UPCOMING,
}

enum class ReminderAction {
    TAKE,
    SNOOZE,
    SKIP,
}

enum class ImageSource {
    CAMERA,
    GALLERY,
    SCAN,
}

enum class HistoryFilter {
    DAY,
    WEEK,
    MONTH,
}

enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

data class OcrSuggestion(
    val rawText: String = "",
    val suggestedName: String = "",
    val suggestedDosage: String = "",
    val suggestedManufacturer: String = "",
    val suggestedInstructions: String = "",
)

data class MedicationDraft(
    val id: Long? = null,
    val name: String = "",
    val dosage: String = "",
    val form: MedicationForm = MedicationForm.TABLET,
    val frequencyLabel: String = "Todos os dias",
    val times: List<LocalTime> = listOf(LocalTime.of(8, 0)),
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val instructions: String = "",
    val notes: String = "",
    val quantityRemaining: Int = 30,
    val accentColor: Long = 0xFFAA8CFF,
    val iconEmoji: String = "💊",
    val imageUri: String? = null,
    val imageSource: ImageSource? = null,
    val manufacturer: String = "",
    val ocrSuggestion: OcrSuggestion? = null,
)

data class NextDoseSnapshot(
    val medicationId: Long = 0L,
    val medicationName: String = "",
    val dosage: String = "",
    val scheduledAt: LocalDateTime = LocalDateTime.now(),
    val remainingMinutes: Long = 0L,
    val progress: Float = 0f,
    val imageUri: String? = null,
    val accentColor: Long = 0xFFAA8CFF,
    val scheduleId: Long? = null,
)

data class DashboardSnapshot(
    val greetingTitle: String = "Próxima dose",
    val nextDose: NextDoseSnapshot? = null,
    val dueTodayCount: Int = 0,
    val pendingTodayCount: Int = 0,
    val activeReminder: ActiveReminderSnapshot? = null,
)

data class MedicationSummary(
    val id: Long,
    val name: String,
    val dosage: String,
    val form: MedicationForm,
    val nextTimeLabel: String,
    val quantityRemaining: Int,
    val accentColor: Long,
    val imageUri: String? = null,
)

data class MedicationDetails(
    val id: Long,
    val name: String,
    val dosage: String,
    val form: MedicationForm,
    val frequencyLabel: String,
    val schedules: List<LocalTime>,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val instructions: String,
    val notes: String,
    val quantityRemaining: Int,
    val accentColor: Long,
    val iconEmoji: String,
    val imageUri: String? = null,
    val manufacturer: String = "",
)

data class DoseLogItemModel(
    val id: Long,
    val medicationId: Long,
    val medicationName: String,
    val dosage: String,
    val scheduledAt: LocalDateTime,
    val actualAt: LocalDateTime?,
    val status: DoseStatus,
    val note: String,
    val accentColor: Long,
)

data class ActiveReminderSnapshot(
    val reminderId: Long,
    val medicationId: Long,
    val medicationName: String,
    val dosage: String,
    val scheduleId: Long?,
    val triggerAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val progress: Float,
)

data class TreatmentProgressSnapshot(
    val medicationId: Long,
    val medicationName: String,
    val dosage: String,
    val dosesTaken: Int,
    val totalDoses: Int,
    val nextDoseAt: LocalDateTime?,
    val accentColor: Long = 0xFFAA8CFF,
) {
    val progressPercent: Int =
        ((dosesTaken.coerceAtLeast(0) * 100f) / totalDoses.coerceAtLeast(1))
            .toInt()
            .coerceIn(0, 100)

    val isComplete: Boolean = dosesTaken >= totalDoses.coerceAtLeast(1)
}

data class SettingsSnapshot(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val dynamicColorEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val liveUpdatesEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val languageTag: String = "system",
    val nowBarColor: Long = 0xFFAA8CFF,
    val nowBarTone: Int = 50,
    val navigationPillTransparency: Int = 8,
)

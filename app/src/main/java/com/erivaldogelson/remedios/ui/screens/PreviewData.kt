package com.erivaldogelson.remedios.ui.screens

import com.erivaldogelson.remedios.domain.model.ActiveReminderSnapshot
import com.erivaldogelson.remedios.domain.model.AppThemeMode
import com.erivaldogelson.remedios.domain.model.DashboardSnapshot
import com.erivaldogelson.remedios.domain.model.DoseLogItemModel
import com.erivaldogelson.remedios.domain.model.DoseStatus
import com.erivaldogelson.remedios.domain.model.MedicationDetails
import com.erivaldogelson.remedios.domain.model.MedicationDraft
import com.erivaldogelson.remedios.domain.model.MedicationForm
import com.erivaldogelson.remedios.domain.model.MedicationSummary
import com.erivaldogelson.remedios.domain.model.NextDoseSnapshot
import com.erivaldogelson.remedios.domain.model.OcrSuggestion
import com.erivaldogelson.remedios.domain.model.SettingsSnapshot
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object PreviewData {
    private val nextDoseTime = LocalDateTime.now().plusMinutes(42)

    val dashboard = DashboardSnapshot(
        nextDose = NextDoseSnapshot(
            medicationId = 1,
            medicationName = "Amoxicilina",
            dosage = "500 mg",
            scheduledAt = nextDoseTime,
            remainingMinutes = 42,
            progress = 0.62f,
            accentColor = 0xFFB999FF,
        ),
        dueTodayCount = 4,
        pendingTodayCount = 2,
        activeReminder = ActiveReminderSnapshot(
            reminderId = 77,
            medicationId = 1,
            medicationName = "Amoxicilina",
            dosage = "500 mg",
            scheduleId = 2,
            triggerAt = LocalDateTime.now().minusMinutes(6),
            expiresAt = LocalDateTime.now().plusMinutes(39),
            progress = 0.13f,
        ),
    )

    val medicationList = listOf(
        MedicationSummary(1, "Amoxicilina", "500 mg", MedicationForm.CAPSULE, "08:00", 18, 0xFFB999FF),
        MedicationSummary(2, "Vitamina C", "1 comprimido", MedicationForm.TABLET, "13:30", 42, 0xFF8FE7C7),
        MedicationSummary(3, "Xarope infantil", "10 ml", MedicationForm.SYRUP, "22:00", 1, 0xFFFFC875),
    )

    val medicationDetails = MedicationDetails(
        id = 1,
        name = "Amoxicilina",
        dosage = "500 mg",
        form = MedicationForm.CAPSULE,
        frequencyLabel = "A cada 8 horas",
        schedules = listOf(LocalTime.of(8, 0), LocalTime.of(16, 0), LocalTime.of(23, 0)),
        startDate = LocalDate.now(),
        endDate = LocalDate.now().plusDays(7),
        instructions = "Tomar após refeição leve e água.",
        notes = "Acompanhar melhora em 48h.",
        quantityRemaining = 18,
        accentColor = 0xFFB999FF,
        iconEmoji = "💊",
        manufacturer = "Saúde Farma",
    )

    val historyItems = listOf(
        DoseLogItemModel(1, 1, "Amoxicilina", "500 mg", LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(2).plusMinutes(8), DoseStatus.TAKEN, "Tomado com água.", 0xFFB999FF),
        DoseLogItemModel(2, 2, "Vitamina C", "1 comprimido", LocalDateTime.now().minusHours(8), null, DoseStatus.MISSED, "Esquecido durante o almoço.", 0xFFFFC875),
        DoseLogItemModel(3, 3, "Xarope infantil", "10 ml", LocalDateTime.now().minusHours(16), null, DoseStatus.SNOOZED, "Adiado para mais tarde.", 0xFF8FE7C7),
    )

    val draft = MedicationDraft(
        name = "Cloridrato de loratadina",
        dosage = "10 mg",
        form = MedicationForm.TABLET,
        frequencyLabel = "Todos os dias",
        times = listOf(LocalTime.of(9, 0)),
        instructions = "Tomar preferencialmente pela manhã.",
        notes = "Evitar perder a dose em dias de crise.",
        quantityRemaining = 12,
        accentColor = 0xFFB999FF,
    )

    val ocrSuggestion = OcrSuggestion(
        rawText = "Loratadina 10mg via oral laboratório Saúde Farma",
        suggestedName = "Loratadina",
        suggestedDosage = "10mg",
        suggestedManufacturer = "Saúde Farma",
        suggestedInstructions = "Via oral",
    )

    val settings = SettingsSnapshot(
        themeMode = AppThemeMode.DARK,
        dynamicColorEnabled = true,
        onboardingCompleted = true,
        liveUpdatesEnabled = true,
        hapticsEnabled = true,
    )
}


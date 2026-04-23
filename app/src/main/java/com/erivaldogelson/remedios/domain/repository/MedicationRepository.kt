package com.erivaldogelson.remedios.domain.repository

import com.erivaldogelson.remedios.domain.model.ActiveReminderSnapshot
import com.erivaldogelson.remedios.domain.model.DashboardSnapshot
import com.erivaldogelson.remedios.domain.model.DoseLogItemModel
import com.erivaldogelson.remedios.domain.model.HistoryFilter
import com.erivaldogelson.remedios.domain.model.MedicationDetails
import com.erivaldogelson.remedios.domain.model.MedicationDraft
import com.erivaldogelson.remedios.domain.model.MedicationSummary
import com.erivaldogelson.remedios.domain.model.ReminderAction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface MedicationRepository {
    fun observeDashboard(): Flow<DashboardSnapshot>
    fun observeMedications(): Flow<List<MedicationSummary>>
    fun observeMedication(id: Long): Flow<MedicationDetails?>
    fun observeHistory(filter: HistoryFilter): Flow<List<DoseLogItemModel>>
    fun observeActiveReminder(): Flow<ActiveReminderSnapshot?>
    suspend fun seedIfEmpty()
    suspend fun upsertMedication(draft: MedicationDraft): Long
    suspend fun recordDoseAction(
        medicationId: Long,
        scheduleId: Long?,
        action: ReminderAction,
        at: LocalDateTime = LocalDateTime.now(),
    )
    suspend fun rescheduleReminders()
}


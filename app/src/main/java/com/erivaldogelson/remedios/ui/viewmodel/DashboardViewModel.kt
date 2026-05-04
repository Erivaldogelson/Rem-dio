package com.erivaldogelson.remedios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erivaldogelson.remedios.domain.model.ActiveReminderSnapshot
import com.erivaldogelson.remedios.domain.model.DashboardSnapshot
import com.erivaldogelson.remedios.domain.model.NextDoseSnapshot
import com.erivaldogelson.remedios.domain.model.ReminderAction
import com.erivaldogelson.remedios.domain.repository.MedicationRepository
import com.erivaldogelson.remedios.notifications.DoseLiveUpdatePayload
import com.erivaldogelson.remedios.notifications.MedicationLiveUpdateManager
import com.erivaldogelson.remedios.notifications.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val medicationRepository: MedicationRepository,
    private val reminderScheduler: ReminderScheduler,
    private val liveUpdateManager: MedicationLiveUpdateManager,
) : ViewModel() {
    val state = medicationRepository.observeDashboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardSnapshot())

    fun takeNow(snapshot: DashboardSnapshot) = onDoseAction(snapshot, ReminderAction.TAKE)

    fun snooze(snapshot: DashboardSnapshot) = onDoseAction(snapshot, ReminderAction.SNOOZE)

    fun skip(snapshot: DashboardSnapshot) = onDoseAction(snapshot, ReminderAction.SKIP)

    private fun onDoseAction(snapshot: DashboardSnapshot, action: ReminderAction) {
        val activeReminder = snapshot.activeReminder
        val dose = snapshot.nextDose
        if (activeReminder == null && dose == null) return
        viewModelScope.launch {
            val payload = activeReminder?.toPayload() ?: dose?.toPayload()
            if (activeReminder != null) {
                medicationRepository.recordDoseAction(
                    medicationId = activeReminder.medicationId,
                    scheduleId = activeReminder.scheduleId,
                    action = action,
                    scheduledAt = activeReminder.triggerAt,
                )
            } else if (dose != null) {
                medicationRepository.recordDoseAction(
                    medicationId = dose.medicationId,
                    scheduleId = dose.scheduleId,
                    action = action,
                    scheduledAt = dose.scheduledAt,
                )
            }
            payload?.let(liveUpdateManager::completeDoseLiveUpdate)
            reminderScheduler.scheduleAllExisting()
        }
    }

    private fun ActiveReminderSnapshot.toPayload(): DoseLiveUpdatePayload =
        DoseLiveUpdatePayload(
            medicationId = medicationId,
            scheduleId = scheduleId,
            medicationName = medicationName,
            dosage = dosage,
            triggerAt = triggerAt,
            expiresAt = expiresAt,
        )

    private fun NextDoseSnapshot.toPayload(): DoseLiveUpdatePayload =
        DoseLiveUpdatePayload(
            medicationId = medicationId,
            scheduleId = scheduleId,
            medicationName = medicationName,
            dosage = dosage,
            triggerAt = scheduledAt,
        )
}

package com.erivaldogelson.remedios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erivaldogelson.remedios.domain.model.NextDoseSnapshot
import com.erivaldogelson.remedios.domain.model.ReminderAction
import com.erivaldogelson.remedios.domain.repository.MedicationRepository
import com.erivaldogelson.remedios.notifications.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val medicationRepository: MedicationRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    val state = medicationRepository.observeDashboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.erivaldogelson.remedios.domain.model.DashboardSnapshot())

    fun takeNow(nextDose: NextDoseSnapshot?) = onDoseAction(nextDose, ReminderAction.TAKE)

    fun snooze(nextDose: NextDoseSnapshot?) = onDoseAction(nextDose, ReminderAction.SNOOZE)

    fun skip(nextDose: NextDoseSnapshot?) = onDoseAction(nextDose, ReminderAction.SKIP)

    private fun onDoseAction(nextDose: NextDoseSnapshot?, action: ReminderAction) {
        val dose = nextDose ?: return
        viewModelScope.launch {
            medicationRepository.recordDoseAction(
                medicationId = dose.medicationId,
                scheduleId = dose.scheduleId,
                action = action,
            )
            reminderScheduler.scheduleAllExisting()
        }
    }
}


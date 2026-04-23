package com.erivaldogelson.remedios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erivaldogelson.remedios.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ActiveReminderViewModel(
    medicationRepository: MedicationRepository,
) : ViewModel() {
    val activeReminder = medicationRepository.observeActiveReminder()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

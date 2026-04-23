package com.erivaldogelson.remedios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erivaldogelson.remedios.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MedicationDetailViewModel(
    medicationId: Long,
    medicationRepository: MedicationRepository,
) : ViewModel() {
    val detail = medicationRepository.observeMedication(medicationId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}


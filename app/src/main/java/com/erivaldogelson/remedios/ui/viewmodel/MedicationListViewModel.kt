package com.erivaldogelson.remedios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erivaldogelson.remedios.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicationListViewModel(
    private val medicationRepository: MedicationRepository,
) : ViewModel() {
    val medications = medicationRepository.observeMedications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteMedication(id: Long) {
        viewModelScope.launch {
            medicationRepository.deleteMedication(id)
        }
    }
}

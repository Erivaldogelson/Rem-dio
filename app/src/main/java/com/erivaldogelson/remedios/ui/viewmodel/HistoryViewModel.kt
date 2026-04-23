@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.erivaldogelson.remedios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erivaldogelson.remedios.domain.model.HistoryFilter
import com.erivaldogelson.remedios.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    private val medicationRepository: MedicationRepository,
) : ViewModel() {
    private val filter = MutableStateFlow(HistoryFilter.DAY)

    val selectedFilter = filter.stateIn(viewModelScope, SharingStarted.Eagerly, HistoryFilter.DAY)
    val items = filter.flatMapLatest { medicationRepository.observeHistory(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectFilter(value: HistoryFilter) {
        filter.value = value
    }
}

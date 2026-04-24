package com.erivaldogelson.remedios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erivaldogelson.remedios.domain.repository.SettingsRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    fun completeOnboarding(onCompleted: () -> Unit) = viewModelScope.launch {
        settingsRepository.completeOnboarding()
        onCompleted()
    }
}

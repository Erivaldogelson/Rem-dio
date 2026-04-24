package com.erivaldogelson.remedios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erivaldogelson.remedios.domain.model.ActiveReminderSnapshot
import com.erivaldogelson.remedios.domain.model.AppThemeMode
import com.erivaldogelson.remedios.domain.model.NextDoseSnapshot
import com.erivaldogelson.remedios.domain.repository.MedicationRepository
import com.erivaldogelson.remedios.domain.repository.SettingsRepository
import com.erivaldogelson.remedios.notifications.DoseLiveUpdatePayload
import com.erivaldogelson.remedios.notifications.MedicationLiveUpdateManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val medicationRepository: MedicationRepository,
    private val liveUpdateManager: MedicationLiveUpdateManager,
) : ViewModel() {
    val settings = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.erivaldogelson.remedios.domain.model.SettingsSnapshot())

    fun setThemeMode(mode: AppThemeMode) = viewModelScope.launch {
        settingsRepository.setThemeMode(mode)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setDynamicColor(enabled)
    }

    fun setLiveUpdates(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setLiveUpdates(enabled)
    }

    fun setHaptics(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setHaptics(enabled)
    }

    fun setLanguageTag(languageTag: String) = viewModelScope.launch {
        settingsRepository.setLanguageTag(languageTag)
    }

    fun setNowBarColor(color: Long) = viewModelScope.launch {
        settingsRepository.setNowBarColor(color)
        refreshLiveUpdateColor()
    }

    fun setNowBarTone(tone: Int) = viewModelScope.launch {
        settingsRepository.setNowBarTone(tone)
        refreshLiveUpdateColor()
    }

    private suspend fun refreshLiveUpdateColor() {
        val snapshot = medicationRepository.observeDashboard().first()
        val payload = snapshot.activeReminder?.toPayload() ?: snapshot.nextDose?.toPayload() ?: return
        liveUpdateManager.updateDoseLiveUpdateProgress(payload)
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

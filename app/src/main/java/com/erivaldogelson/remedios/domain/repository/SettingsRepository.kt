package com.erivaldogelson.remedios.domain.repository

import com.erivaldogelson.remedios.domain.model.AppThemeMode
import com.erivaldogelson.remedios.domain.model.SettingsSnapshot
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<SettingsSnapshot>
    suspend fun completeOnboarding()
    suspend fun setThemeMode(mode: AppThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setLiveUpdates(enabled: Boolean)
    suspend fun setHaptics(enabled: Boolean)
}


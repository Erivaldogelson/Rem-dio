package com.erivaldogelson.remedios.data.repository

import com.erivaldogelson.remedios.data.local.dao.SettingsDao
import com.erivaldogelson.remedios.data.local.entity.AppSettingsEntity
import com.erivaldogelson.remedios.data.preferences.UserPreferencesRepository
import com.erivaldogelson.remedios.domain.model.AppThemeMode
import com.erivaldogelson.remedios.domain.model.SettingsSnapshot
import com.erivaldogelson.remedios.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalDateTime

class SettingsRepositoryImpl(
    private val preferencesRepository: UserPreferencesRepository,
    private val settingsDao: SettingsDao,
    private val clock: Clock = Clock.systemDefaultZone(),
) : SettingsRepository {

    override val settings: Flow<SettingsSnapshot> = preferencesRepository.settings

    override suspend fun completeOnboarding() {
        preferencesRepository.completeOnboarding()
        persistSnapshot(preferencesRepository.settingsValue())
    }

    override suspend fun setThemeMode(mode: AppThemeMode) {
        preferencesRepository.setThemeMode(mode)
        persistSnapshot(preferencesRepository.settingsValue().copy(themeMode = mode))
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        preferencesRepository.setDynamicColor(enabled)
        persistSnapshot(preferencesRepository.settingsValue().copy(dynamicColorEnabled = enabled))
    }

    override suspend fun setLiveUpdates(enabled: Boolean) {
        preferencesRepository.setLiveUpdates(enabled)
        persistSnapshot(preferencesRepository.settingsValue().copy(liveUpdatesEnabled = enabled))
    }

    override suspend fun setHaptics(enabled: Boolean) {
        preferencesRepository.setHaptics(enabled)
        persistSnapshot(preferencesRepository.settingsValue().copy(hapticsEnabled = enabled))
    }

    override suspend fun setLanguageTag(languageTag: String) {
        preferencesRepository.setLanguageTag(languageTag)
        persistSnapshot(preferencesRepository.settingsValue().copy(languageTag = languageTag))
    }

    override suspend fun setNowBarColor(color: Long) {
        preferencesRepository.setNowBarColor(color)
    }

    override suspend fun setNowBarTone(tone: Int) {
        preferencesRepository.setNowBarTone(tone)
    }

    override suspend fun setNavigationPillTransparency(transparency: Int) {
        preferencesRepository.setNavigationPillTransparency(transparency)
    }

    private suspend fun persistSnapshot(snapshot: SettingsSnapshot) {
        settingsDao.upsert(
            AppSettingsEntity(
                themeMode = snapshot.themeMode.name,
                dynamicColorEnabled = snapshot.dynamicColorEnabled,
                onboardingCompleted = snapshot.onboardingCompleted,
                liveUpdatesEnabled = snapshot.liveUpdatesEnabled,
                hapticsEnabled = snapshot.hapticsEnabled,
                languageTag = snapshot.languageTag,
                updatedAt = LocalDateTime.now(clock),
            ),
        )
    }
}

package com.erivaldogelson.remedios.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.erivaldogelson.remedios.domain.model.AppThemeMode
import com.erivaldogelson.remedios.domain.model.SettingsSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dataStore = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = { context.filesDir.resolve("user-preferences.preferences_pb") },
    )

    private object Keys {
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val themeMode = stringPreferencesKey("theme_mode")
        val liveUpdates = booleanPreferencesKey("live_updates")
        val haptics = booleanPreferencesKey("haptics")
        val languageTag = stringPreferencesKey("language_tag")
    }

    val settings: Flow<SettingsSnapshot> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { prefs -> prefs.toSettings() }

    suspend fun completeOnboarding() = dataStore.edit {
        it[Keys.onboardingCompleted] = true
    }

    suspend fun setThemeMode(mode: AppThemeMode) = dataStore.edit {
        it[Keys.themeMode] = mode.name
    }

    suspend fun setDynamicColor(enabled: Boolean) = dataStore.edit {
        it[Keys.dynamicColor] = enabled
    }

    suspend fun setLiveUpdates(enabled: Boolean) = dataStore.edit {
        it[Keys.liveUpdates] = enabled
    }

    suspend fun setHaptics(enabled: Boolean) = dataStore.edit {
        it[Keys.haptics] = enabled
    }

    suspend fun settingsValue(): SettingsSnapshot = settings.first()

    private fun Preferences.toSettings(): SettingsSnapshot = SettingsSnapshot(
        themeMode = this[Keys.themeMode]
            ?.let { value -> runCatching { AppThemeMode.valueOf(value) }.getOrNull() }
            ?: AppThemeMode.DARK,
        dynamicColorEnabled = this[Keys.dynamicColor] ?: true,
        onboardingCompleted = this[Keys.onboardingCompleted] ?: false,
        liveUpdatesEnabled = this[Keys.liveUpdates] ?: true,
        hapticsEnabled = this[Keys.haptics] ?: true,
        languageTag = this[Keys.languageTag] ?: "pt-BR",
    )
}

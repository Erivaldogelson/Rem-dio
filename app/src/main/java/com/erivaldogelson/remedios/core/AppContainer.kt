package com.erivaldogelson.remedios.core

import android.content.Context
import androidx.room.Room
import com.erivaldogelson.remedios.auth.AuthRepository
import com.erivaldogelson.remedios.auth.SessionManager
import com.erivaldogelson.remedios.data.local.AppDatabase
import com.erivaldogelson.remedios.data.preferences.UserPreferencesRepository
import com.erivaldogelson.remedios.data.repository.MedicationRepositoryImpl
import com.erivaldogelson.remedios.data.repository.SettingsRepositoryImpl
import com.erivaldogelson.remedios.domain.repository.MedicationRepository
import com.erivaldogelson.remedios.domain.repository.SettingsRepository
import com.erivaldogelson.remedios.media.MedicationImageManager
import com.erivaldogelson.remedios.network.ApiClient
import com.erivaldogelson.remedios.network.ApiService
import com.erivaldogelson.remedios.notifications.MedicationLiveUpdateManager
import com.erivaldogelson.remedios.notifications.ReminderScheduler
import com.erivaldogelson.remedios.ocr.MedicationOcrParser
import com.erivaldogelson.remedios.ocr.MedicationTextRecognizer
import com.erivaldogelson.remedios.security.DeviceIntegrityChecker
import com.erivaldogelson.remedios.security.SecurePrefsManager
import com.erivaldogelson.remedios.widgets.MedicationWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Clock

interface AppContainer {
    val medicationRepository: MedicationRepository
    val settingsRepository: SettingsRepository
    val imageManager: MedicationImageManager
    val textRecognizer: MedicationTextRecognizer
    val liveUpdateManager: MedicationLiveUpdateManager
    val reminderScheduler: ReminderScheduler
    val securePrefsManager: SecurePrefsManager
    val deviceIntegrityChecker: DeviceIntegrityChecker
    val sessionManager: SessionManager
    val apiService: ApiService
    val authRepository: AuthRepository
    suspend fun bootstrap()
}

class DefaultAppContainer(
    private val context: Context,
    private val clock: Clock = Clock.systemDefaultZone(),
) : AppContainer {
    private val database by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "remedios.db",
        ).fallbackToDestructiveMigration().build()
    }

    private val preferencesRepository by lazy { UserPreferencesRepository(context) }
    private val ocrParser by lazy { MedicationOcrParser() }

    override val securePrefsManager: SecurePrefsManager by lazy { SecurePrefsManager(context) }
    override val deviceIntegrityChecker: DeviceIntegrityChecker by lazy { DeviceIntegrityChecker(context) }
    override val sessionManager: SessionManager by lazy { SessionManager(securePrefsManager) }
    override val apiService: ApiService by lazy {
        ApiClient.create(
            tokenProvider = sessionManager,
            authFailureHandler = sessionManager,
        )
    }
    override val authRepository: AuthRepository by lazy {
        AuthRepository(
            apiService = apiService,
            sessionManager = sessionManager,
            deviceIntegrityChecker = deviceIntegrityChecker,
        )
    }
    override val imageManager: MedicationImageManager by lazy { MedicationImageManager(context) }
    override val textRecognizer: MedicationTextRecognizer by lazy {
        MedicationTextRecognizer(context, ocrParser)
    }
    override val liveUpdateManager: MedicationLiveUpdateManager by lazy {
        MedicationLiveUpdateManager(context, preferencesRepository)
    }
    override val reminderScheduler: ReminderScheduler by lazy {
        ReminderScheduler(
            context = context,
            reminderDao = database.reminderDao(),
            medicationDao = database.medicationDao(),
        )
    }
    override val medicationRepository: MedicationRepository by lazy {
        MedicationRepositoryImpl(
            medicationDao = database.medicationDao(),
            doseLogDao = database.doseLogDao(),
            reminderDao = database.reminderDao(),
            clock = clock,
            onCancelLiveUpdate = liveUpdateManager::cancelDoseLiveUpdate,
            onMedicationDataChanged = {
                withContext(Dispatchers.IO) {
                    MedicationWidgetProvider.updateAll(context)
                }
            },
        )
    }
    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(
            preferencesRepository = preferencesRepository,
            settingsDao = database.settingsDao(),
            clock = clock,
        )
    }

    override suspend fun bootstrap() {
        liveUpdateManager.ensureChannel()
        medicationRepository.seedIfEmpty()
        medicationRepository.rescheduleReminders()
        reminderScheduler.scheduleAllExisting()
        reminderScheduler.enqueuePeriodicRefresh()
        MedicationWidgetProvider.updateAll(context)
    }
}

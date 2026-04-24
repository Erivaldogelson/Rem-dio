package com.erivaldogelson.remedios.core

import android.content.Context
import androidx.room.Room
import com.erivaldogelson.remedios.data.local.AppDatabase
import com.erivaldogelson.remedios.data.preferences.UserPreferencesRepository
import com.erivaldogelson.remedios.data.repository.MedicationRepositoryImpl
import com.erivaldogelson.remedios.data.repository.SettingsRepositoryImpl
import com.erivaldogelson.remedios.domain.repository.MedicationRepository
import com.erivaldogelson.remedios.domain.repository.SettingsRepository
import com.erivaldogelson.remedios.media.MedicationImageManager
import com.erivaldogelson.remedios.notifications.MedicationLiveUpdateManager
import com.erivaldogelson.remedios.notifications.ReminderScheduler
import com.erivaldogelson.remedios.ocr.MedicationOcrParser
import com.erivaldogelson.remedios.ocr.MedicationTextRecognizer
import java.time.Clock

interface AppContainer {
    val medicationRepository: MedicationRepository
    val settingsRepository: SettingsRepository
    val imageManager: MedicationImageManager
    val textRecognizer: MedicationTextRecognizer
    val liveUpdateManager: MedicationLiveUpdateManager
    val reminderScheduler: ReminderScheduler
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

    override val imageManager: MedicationImageManager by lazy { MedicationImageManager(context) }
    override val textRecognizer: MedicationTextRecognizer by lazy {
        MedicationTextRecognizer(context, ocrParser)
    }
    override val liveUpdateManager: MedicationLiveUpdateManager by lazy {
        MedicationLiveUpdateManager(context)
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
    }
}

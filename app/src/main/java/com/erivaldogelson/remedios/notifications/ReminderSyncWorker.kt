package com.erivaldogelson.remedios.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.erivaldogelson.remedios.core.appContainer

class ReminderSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = runCatching {
        applicationContext.appContainer.medicationRepository.seedIfEmpty()
        applicationContext.appContainer.medicationRepository.rescheduleReminders()
        applicationContext.appContainer.reminderScheduler.scheduleAllExisting()
    }.fold(
        onSuccess = { Result.success() },
        onFailure = { Result.retry() },
    )
}

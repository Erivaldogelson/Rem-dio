package com.erivaldogelson.remedios.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.erivaldogelson.remedios.core.appContainer
import kotlinx.coroutines.runBlocking

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val payload = intent.getDoseLiveUpdatePayload() ?: return
        val event = intent.getStringExtra(ReminderScheduler.EXTRA_EVENT) ?: ReminderScheduler.EVENT_DUE

        runBlocking {
            val container = context.appContainer
            if (!container.medicationRepository.medicationExists(payload.medicationId)) {
                container.liveUpdateManager.cancelDoseLiveUpdate(payload)
                return@runBlocking
            }
            when (event) {
                ReminderScheduler.EVENT_LIVE_START -> {
                }
                ReminderScheduler.EVENT_PROGRESS_TICK -> {
                }
                ReminderScheduler.EVENT_DUE -> {
                    container.medicationRepository.activateReminder(
                        medicationId = payload.medicationId,
                        scheduleId = payload.scheduleId,
                        triggerAt = payload.triggerAt,
                    )
                    container.liveUpdateManager.showDoseReminder(payload)
                }
                ReminderScheduler.EVENT_EXPIRE -> {
                    container.medicationRepository.expireReminder(
                        medicationId = payload.medicationId,
                        scheduleId = payload.scheduleId,
                        triggerAt = payload.triggerAt,
                    )
                    container.liveUpdateManager.cancelDoseReminder(payload)
                    container.reminderScheduler.scheduleAllExisting()
                }
            }
        }
    }
}

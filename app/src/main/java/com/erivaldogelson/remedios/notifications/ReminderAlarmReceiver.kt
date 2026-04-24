package com.erivaldogelson.remedios.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.erivaldogelson.remedios.core.appContainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val payload = intent.getDoseLiveUpdatePayload() ?: return
        val event = intent.getStringExtra(ReminderScheduler.EXTRA_EVENT) ?: ReminderScheduler.EVENT_DUE

        runBlocking {
            val container = context.appContainer
            val liveUpdatesEnabled = container.settingsRepository.settings.first().liveUpdatesEnabled
            when (event) {
                ReminderScheduler.EVENT_LIVE_START -> {
                    if (liveUpdatesEnabled) {
                        container.liveUpdateManager.startDoseLiveUpdate(payload)
                        container.reminderScheduler.scheduleLiveUpdateProgressTick(payload)
                    }
                }
                ReminderScheduler.EVENT_PROGRESS_TICK -> {
                    if (liveUpdatesEnabled) {
                        container.liveUpdateManager.updateDoseLiveUpdateProgress(payload)
                        container.reminderScheduler.scheduleLiveUpdateProgressTick(payload)
                    }
                }
                ReminderScheduler.EVENT_DUE -> {
                    container.medicationRepository.activateReminder(
                        medicationId = payload.medicationId,
                        scheduleId = payload.scheduleId,
                        triggerAt = payload.triggerAt,
                    )
                    container.liveUpdateManager.startDoseLiveUpdate(payload)
                }
                ReminderScheduler.EVENT_EXPIRE -> {
                    container.medicationRepository.expireReminder(
                        medicationId = payload.medicationId,
                        scheduleId = payload.scheduleId,
                        triggerAt = payload.triggerAt,
                    )
                    container.liveUpdateManager.cancelDoseLiveUpdate(payload)
                    container.reminderScheduler.scheduleAllExisting()
                }
            }
        }
    }
}

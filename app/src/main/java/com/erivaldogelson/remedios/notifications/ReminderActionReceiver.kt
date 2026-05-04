package com.erivaldogelson.remedios.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.erivaldogelson.remedios.core.appContainer
import com.erivaldogelson.remedios.domain.model.ReminderAction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val payload = intent.getDoseLiveUpdatePayload() ?: return

        val action = when (intent.action) {
            MedicationLiveUpdateManager.ACTION_TAKE -> ReminderAction.TAKE
            MedicationLiveUpdateManager.ACTION_SNOOZE -> ReminderAction.SNOOZE
            MedicationLiveUpdateManager.ACTION_SKIP -> ReminderAction.SKIP
            else -> return
        }

        runBlocking {
            val container = context.appContainer
            if (!container.medicationRepository.medicationExists(payload.medicationId)) {
                container.liveUpdateManager.cancelDoseLiveUpdate(payload)
                return@runBlocking
            }
            container.medicationRepository.recordDoseAction(
                medicationId = payload.medicationId,
                scheduleId = payload.scheduleId,
                action = action,
                scheduledAt = payload.triggerAt,
            )
            container.liveUpdateManager.cancelDoseReminder(payload.notificationId)
            if (action == ReminderAction.TAKE && container.settingsRepository.settings.first().liveUpdatesEnabled) {
                container.medicationRepository.treatmentProgressFor(payload.medicationId)?.let { progress ->
                    container.liveUpdateManager.startTreatmentLiveUpdate(progress)
                }
            }
            container.reminderScheduler.scheduleAllExisting()
        }
    }
}

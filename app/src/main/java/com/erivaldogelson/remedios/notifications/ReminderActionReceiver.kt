package com.erivaldogelson.remedios.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.erivaldogelson.remedios.core.appContainer
import com.erivaldogelson.remedios.domain.model.ReminderAction
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
            context.appContainer.medicationRepository.recordDoseAction(
                medicationId = payload.medicationId,
                scheduleId = payload.scheduleId,
                action = action,
                scheduledAt = payload.triggerAt,
            )
            context.appContainer.reminderScheduler.scheduleAllExisting()
        }
        context.appContainer.liveUpdateManager.completeDoseLiveUpdate(payload)
    }
}

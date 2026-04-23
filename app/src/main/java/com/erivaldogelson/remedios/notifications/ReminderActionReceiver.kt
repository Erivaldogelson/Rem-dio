package com.erivaldogelson.remedios.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.erivaldogelson.remedios.core.appContainer
import com.erivaldogelson.remedios.domain.model.ReminderAction
import kotlinx.coroutines.runBlocking

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(ReminderNotifier.EXTRA_MEDICATION_ID, -1L)
        if (medicationId < 0) return

        val action = when (intent.action) {
            ReminderNotifier.ACTION_TAKE -> ReminderAction.TAKE
            ReminderNotifier.ACTION_SNOOZE -> ReminderAction.SNOOZE
            ReminderNotifier.ACTION_SKIP -> ReminderAction.SKIP
            else -> return
        }
        val scheduleId = intent.getLongExtra(ReminderNotifier.EXTRA_SCHEDULE_ID, Long.MIN_VALUE)
            .takeIf { it != Long.MIN_VALUE }
        val notificationId = intent.getIntExtra(ReminderNotifier.EXTRA_NOTIFICATION_ID, medicationId.toInt())

        runBlocking {
            context.appContainer.medicationRepository.recordDoseAction(
                medicationId = medicationId,
                scheduleId = scheduleId,
                action = action,
            )
            context.appContainer.reminderScheduler.scheduleAllExisting()
        }
        context.appContainer.reminderNotifier.cancelNotification(notificationId)
    }
}


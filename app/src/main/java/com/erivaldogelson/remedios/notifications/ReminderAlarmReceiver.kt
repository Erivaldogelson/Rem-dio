package com.erivaldogelson.remedios.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.erivaldogelson.remedios.core.appContainer
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(ReminderNotifier.EXTRA_MEDICATION_ID, -1L)
        if (medicationId < 0) return

        val notificationId = intent.getIntExtra(ReminderNotifier.EXTRA_NOTIFICATION_ID, medicationId.toInt())
        val triggerAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(intent.getLongExtra(ReminderScheduler.EXTRA_TRIGGER_AT, System.currentTimeMillis())),
            ZoneId.systemDefault(),
        )
        val name = intent.getStringExtra(ReminderScheduler.EXTRA_MEDICATION_NAME).orEmpty()
        val dosage = intent.getStringExtra(ReminderScheduler.EXTRA_DOSAGE).orEmpty()
        val scheduleId = intent.getLongExtra(ReminderNotifier.EXTRA_SCHEDULE_ID, Long.MIN_VALUE)
            .takeIf { it != Long.MIN_VALUE }

        runBlocking {
            context.appContainer.reminderNotifier.showReminderNotification(
                ReminderNotificationPayload(
                    medicationId = medicationId,
                    scheduleId = scheduleId,
                    medicationName = name,
                    dosage = dosage,
                    triggerAt = triggerAt,
                    notificationId = notificationId,
                    promotedOngoing = true,
                    progressPercent = 0,
                ),
            )
        }
    }
}


package com.erivaldogelson.remedios.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.erivaldogelson.remedios.MainActivity
import com.erivaldogelson.remedios.R
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.abs

class ReminderNotifier(private val context: Context) {

    fun ensureChannel() {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    fun showReminderNotification(payload: ReminderNotificationPayload) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(payload.medicationName)
            .setContentText("Dose ${payload.dosage} pronta para registrar.")
            .setSubText("Lembrete ativo")
            .setContentIntent(buildContentIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setOngoing(payload.promotedOngoing)
            .setWhen(payload.triggerAt.toEpochMillis())
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(false)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Registre a dose de ${payload.medicationName} agora, adie por 15 minutos ou pule se necessário.",
                ),
            )
            .setProgress(100, payload.progressPercent.coerceIn(0, 100), false)
            .setRequestPromotedOngoing(payload.promotedOngoing)
            .setShortCriticalText(payload.toChipText())
            .addAction(
                0,
                context.getString(R.string.action_take_now),
                actionPendingIntent(payload, ACTION_TAKE),
            )
            .addAction(
                0,
                context.getString(R.string.action_snooze),
                actionPendingIntent(payload, ACTION_SNOOZE),
            )
            .addAction(
                0,
                context.getString(R.string.action_skip),
                actionPendingIntent(payload, ACTION_SKIP),
            )
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(context).notify(payload.notificationId, notification)
    }

    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun buildContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun actionPendingIntent(
        payload: ReminderNotificationPayload,
        action: String,
    ): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_MEDICATION_ID, payload.medicationId)
            putExtra(EXTRA_SCHEDULE_ID, payload.scheduleId)
            putExtra(EXTRA_NOTIFICATION_ID, payload.notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            payload.notificationId + action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun ReminderNotificationPayload.toChipText(): String {
        val delta = Duration.between(LocalDateTime.now(), triggerAt).toMinutes()
        return when {
            abs(delta) <= 1 -> "agora"
            delta > 0 -> "${delta.coerceAtMost(99)}m"
            else -> "ativo"
        }
    }

    private fun LocalDateTime.toEpochMillis(): Long =
        atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    companion object {
        const val CHANNEL_ID = "medication_reminders"
        const val ACTION_TAKE = "com.erivaldogelson.remedios.action.TAKE"
        const val ACTION_SNOOZE = "com.erivaldogelson.remedios.action.SNOOZE"
        const val ACTION_SKIP = "com.erivaldogelson.remedios.action.SKIP"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}


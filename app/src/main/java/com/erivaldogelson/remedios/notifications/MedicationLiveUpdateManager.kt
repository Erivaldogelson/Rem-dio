package com.erivaldogelson.remedios.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.erivaldogelson.remedios.MainActivity
import com.erivaldogelson.remedios.R
import com.erivaldogelson.remedios.data.preferences.UserPreferencesRepository
import com.erivaldogelson.remedios.domain.model.TreatmentProgressSnapshot
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.runBlocking

class MedicationLiveUpdateManager(
    private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showDoseReminder(payload: DoseLiveUpdatePayload) {
        if (!canPostNotifications()) return
        ensureChannel()
        notificationManager.notify(payload.notificationId, buildDoseReminderNotification(payload))
    }

    fun updateDoseReminder(payload: DoseLiveUpdatePayload) {
        showDoseReminder(payload)
    }

    fun completeDoseLiveUpdate(payload: DoseLiveUpdatePayload) {
        cancelDoseReminder(payload.notificationId)
    }

    fun cancelDoseReminder(payload: DoseLiveUpdatePayload) {
        cancelDoseReminder(payload.notificationId)
    }

    fun cancelDoseReminder(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun cancelDoseLiveUpdate(payload: DoseLiveUpdatePayload) {
        cancelDoseLiveUpdate(payload.notificationId)
    }

    fun cancelDoseLiveUpdate(notificationId: Int) {
        notificationManager.cancel(notificationId)
        stopTreatmentLiveUpdate()
    }

    fun startTreatmentLiveUpdate(snapshot: TreatmentProgressSnapshot) {
        startTreatmentLiveUpdate(TreatmentLiveUpdatePayload.from(snapshot))
    }

    fun updateTreatmentLiveUpdate(snapshot: TreatmentProgressSnapshot) {
        startTreatmentLiveUpdate(TreatmentLiveUpdatePayload.from(snapshot))
    }

    fun startTreatmentLiveUpdate(payload: TreatmentLiveUpdatePayload) {
        if (payload.isComplete) {
            stopTreatmentLiveUpdate(payload.medicationId)
            return
        }
        ensureChannel()
        val intent = DoseLiveService.intent(context, DoseLiveService.ACTION_START, payload)
        runCatching {
            ContextCompat.startForegroundService(context, intent)
        }.onFailure {
            if (canPostNotifications()) {
                notificationManager.notify(
                    DoseLiveService.FOREGROUND_NOTIFICATION_ID,
                    buildTreatmentLiveNotification(payload),
                )
            }
        }
    }

    fun stopTreatmentLiveUpdate(medicationId: Long = -1L) {
        val intent = DoseLiveService.stopIntent(context, medicationId)
        runCatching { context.startService(intent) }
        notificationManager.cancel(DoseLiveService.FOREGROUND_NOTIFICATION_ID)
    }

    fun buildTreatmentLiveNotification(payload: TreatmentLiveUpdatePayload): Notification =
        if (Build.VERSION.SDK_INT >= 36) {
            buildAndroid16TreatmentLiveNotification(payload)
        } else {
            buildCompatTreatmentLiveNotification(payload)
        }

    @RequiresApi(36)
    private fun buildAndroid16TreatmentLiveNotification(payload: TreatmentLiveUpdatePayload): Notification {
        val accent = notificationAccentColor(payload.accentColor)
        val icon = Icon.createWithResource(context, R.drawable.ic_pill)
        val style = Notification.ProgressStyle()
            .setProgress(payload.dosesTaken.coerceAtLeast(0))
            .setStyledByProgress(true)
            .setProgressStartIcon(icon)
            .setProgressTrackerIcon(icon)
            .setProgressEndIcon(icon)
            .addProgressSegment(
                Notification.ProgressStyle.Segment(payload.totalDoses.coerceAtLeast(1)).setColor(accent),
            )

        return Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pill)
            .setColor(accent)
            .setColorized(false)
            .setContentTitle("Tratamento em progresso")
            .setContentText(payload.treatmentContentText())
            .setSubText("Now Bar")
            .setContentIntent(buildContentIntent())
            .setPriority(Notification.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setShowWhen(false)
            .setShortCriticalText("${payload.progressPercent}%")
            .setStyle(style)
            .addExtras(Bundle().apply { putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true) })
            .build()
    }

    private fun buildCompatTreatmentLiveNotification(payload: TreatmentLiveUpdatePayload): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pill)
            .setColor(notificationAccentColor(payload.accentColor))
            .setColorized(false)
            .setContentTitle("Tratamento em progresso")
            .setContentText(payload.treatmentContentText())
            .setSubText("Now Bar")
            .setContentIntent(buildContentIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setProgress(payload.totalDoses.coerceAtLeast(1), payload.dosesTaken.coerceAtLeast(0), false)
            .setRequestPromotedOngoing(true)
            .setShortCriticalText("${payload.progressPercent}%")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "${payload.treatmentContentText()} • Progresso: ${payload.progressPercent}%",
                ),
            )
            .build()

    private fun buildDoseReminderNotification(payload: DoseLiveUpdatePayload): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pill)
            .setColor(notificationAccentColor())
            .setColorized(false)
            .setContentTitle("Hora de tomar ${payload.medicationName}")
            .setContentText(payload.doseReminderContentText())
            .setSubText("Lembrete")
            .setContentIntent(buildContentIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setOngoing(false)
            .setWhen(payload.triggerAt.toEpochMillis())
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(payload.triggerAt.isAfter(LocalDateTime.now()))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "${payload.doseReminderContentText()}. Toque em Tomei para iniciar o acompanhamento do tratamento.",
                ),
            )
            .addAction(0, context.getString(R.string.action_take_now), actionPendingIntent(payload, ACTION_TAKE))
            .addAction(0, context.getString(R.string.action_snooze), actionPendingIntent(payload, ACTION_SNOOZE))
            .addAction(0, context.getString(R.string.action_skip), actionPendingIntent(payload, ACTION_SKIP))
            .build()

    private fun notificationAccentColor(fallbackColor: Long = 0xFFAA8CFF): Int =
        runCatching {
            runBlocking {
                val settings = preferencesRepository.settingsValue()
                applyTone(settings.nowBarColor, settings.nowBarTone)
            }
        }.getOrElse {
            applyTone(fallbackColor, 50)
        }

    private fun applyTone(color: Long, tone: Int): Int {
        val clampedTone = tone.coerceIn(0, 100)
        val argb = color.toInt()
        val red = (argb shr 16) and 0xFF
        val green = (argb shr 8) and 0xFF
        val blue = argb and 0xFF
        val target = if (clampedTone < 50) 255 else 0
        val fraction = kotlin.math.abs(clampedTone - 50) / 50f
        fun blend(channel: Int): Int = (channel + ((target - channel) * fraction)).toInt().coerceIn(0, 255)
        return android.graphics.Color.rgb(blend(red), blend(green), blend(blue))
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
        payload: DoseLiveUpdatePayload,
        action: String,
    ): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            this.action = action
            putDoseLiveUpdatePayload(payload)
        }
        return PendingIntent.getBroadcast(
            context,
            payload.notificationId + action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun TreatmentLiveUpdatePayload.treatmentContentText(): String =
        "Dose $dosesTaken de $totalDoses tomada • Próxima dose: ${nextDoseLabel()}"

    private fun DoseLiveUpdatePayload.doseReminderContentText(): String =
        "$dosage às ${timeLabel()} • ${remainingLabel()}"

    private fun DoseLiveUpdatePayload.remainingLabel(now: LocalDateTime = LocalDateTime.now()): String {
        val minutes = Duration.between(now, triggerAt).toMinutes()
        return when {
            minutes > 1 -> "faltam $minutes min"
            minutes == 1L -> "falta 1 min"
            minutes >= 0 -> "é agora"
            now.isBefore(expiresAt) -> "em andamento"
            else -> "encerrada"
        }
    }

    private fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
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
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_TRIGGER_AT = "extra_trigger_at"
        const val EXTRA_LIVE_UPDATE_START_AT = "extra_live_update_start_at"
        const val EXTRA_EXPIRES_AT = "extra_expires_at"

        private const val EXTRA_REQUEST_PROMOTED_ONGOING = "android.requestPromotedOngoing"
    }
}

fun Intent.putDoseLiveUpdatePayload(payload: DoseLiveUpdatePayload): Intent = apply {
    putExtra(MedicationLiveUpdateManager.EXTRA_MEDICATION_ID, payload.medicationId)
    putExtra(MedicationLiveUpdateManager.EXTRA_SCHEDULE_ID, payload.scheduleId)
    putExtra(MedicationLiveUpdateManager.EXTRA_NOTIFICATION_ID, payload.notificationId)
    putExtra(MedicationLiveUpdateManager.EXTRA_MEDICATION_NAME, payload.medicationName)
    putExtra(MedicationLiveUpdateManager.EXTRA_DOSAGE, payload.dosage)
    putExtra(MedicationLiveUpdateManager.EXTRA_TRIGGER_AT, payload.triggerAt.toEpochMillis())
    putExtra(MedicationLiveUpdateManager.EXTRA_LIVE_UPDATE_START_AT, payload.liveUpdateStartAt.toEpochMillis())
    putExtra(MedicationLiveUpdateManager.EXTRA_EXPIRES_AT, payload.expiresAt.toEpochMillis())
}

fun Intent.getDoseLiveUpdatePayload(): DoseLiveUpdatePayload? {
    val medicationId = getLongExtra(MedicationLiveUpdateManager.EXTRA_MEDICATION_ID, -1L)
    if (medicationId < 0) return null
    val triggerMillis = getLongExtra(MedicationLiveUpdateManager.EXTRA_TRIGGER_AT, 0L)
    if (triggerMillis == 0L) return null
    val triggerAt = triggerMillis.toLocalDateTime()
    return DoseLiveUpdatePayload(
        medicationId = medicationId,
        scheduleId = getLongExtra(MedicationLiveUpdateManager.EXTRA_SCHEDULE_ID, Long.MIN_VALUE)
            .takeIf { it != Long.MIN_VALUE },
        medicationName = getStringExtra(MedicationLiveUpdateManager.EXTRA_MEDICATION_NAME).orEmpty(),
        dosage = getStringExtra(MedicationLiveUpdateManager.EXTRA_DOSAGE).orEmpty(),
        triggerAt = triggerAt,
        liveUpdateStartAt = getLongExtra(
            MedicationLiveUpdateManager.EXTRA_LIVE_UPDATE_START_AT,
            triggerAt.minusMinutes(15).toEpochMillis(),
        ).toLocalDateTime(),
        expiresAt = getLongExtra(
            MedicationLiveUpdateManager.EXTRA_EXPIRES_AT,
            triggerAt.plusMinutes(45).toEpochMillis(),
        ).toLocalDateTime(),
        notificationId = getIntExtra(
            MedicationLiveUpdateManager.EXTRA_NOTIFICATION_ID,
            DoseLiveUpdatePayload.notificationId(medicationId, null, triggerAt),
        ),
    )
}

private fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(this), ZoneId.systemDefault())

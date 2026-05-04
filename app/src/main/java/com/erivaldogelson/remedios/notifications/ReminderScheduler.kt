package com.erivaldogelson.remedios.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.erivaldogelson.remedios.data.local.dao.MedicationDao
import com.erivaldogelson.remedios.data.local.dao.ReminderDao
import com.erivaldogelson.remedios.data.local.entity.ReminderEntity
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class ReminderScheduler(
    private val context: Context,
    private val reminderDao: ReminderDao,
    private val medicationDao: MedicationDao,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun scheduleAllExisting() {
        val now = LocalDateTime.now()
        reminderDao.getAll().forEach { reminder ->
            val medication = medicationDao.getMedicationById(reminder.medicationId) ?: return@forEach
            val payload = DoseLiveUpdatePayload(
                medicationId = reminder.medicationId,
                scheduleId = reminder.scheduleId,
                medicationName = medication.name,
                dosage = medication.dosage,
                triggerAt = reminder.triggerAt,
                expiresAt = reminder.expiresAt,
            )
            if (reminder.expiresAt > now) {
                val liveStartAt = reminder.triggerAt.minusMinutes(LIVE_UPDATE_START_WINDOW_MINUTES.toLong())
                if (reminder.triggerAt > now) {
                    scheduleReminderAlarm(
                        payload = payload,
                        event = EVENT_LIVE_START,
                        fireAt = if (liveStartAt.isBefore(now)) now.plusSeconds(2) else liveStartAt,
                    )
                }
                scheduleReminderAlarm(
                    payload = payload,
                    event = EVENT_DUE,
                    fireAt = if (reminder.triggerAt.isBefore(now)) now.plusSeconds(2) else reminder.triggerAt,
                )
                scheduleReminderAlarm(payload, EVENT_EXPIRE, reminder.expiresAt)
            }
        }
    }

    suspend fun nextLiveUpdatePayloadForMedication(medicationId: Long): DoseLiveUpdatePayload? {
        val now = LocalDateTime.now()
        val medication = medicationDao.getMedicationById(medicationId) ?: return null
        val reminder = reminderDao.getAll()
            .filter { it.medicationId == medicationId && it.expiresAt > now }
            .minByOrNull { it.triggerAt }
            ?: return null

        return DoseLiveUpdatePayload(
            medicationId = reminder.medicationId,
            scheduleId = reminder.scheduleId,
            medicationName = medication.name,
            dosage = medication.dosage,
            triggerAt = reminder.triggerAt,
            liveUpdateStartAt = now,
            expiresAt = reminder.expiresAt,
        )
    }

    fun scheduleLiveUpdateProgressTick(payload: DoseLiveUpdatePayload) {
        val now = LocalDateTime.now()
        if (!now.isBefore(payload.triggerAt)) return
        scheduleReminderAlarm(
            payload = payload,
            event = EVENT_PROGRESS_TICK,
            fireAt = now.plusMinutes(LIVE_UPDATE_PROGRESS_TICK_MINUTES.toLong()),
            exact = false,
        )
    }

    fun enqueuePeriodicRefresh() {
        val request = PeriodicWorkRequestBuilder<ReminderSyncWorker>(6, TimeUnit.HOURS)
            .setInitialDelay(30, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_SYNC_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    companion object {
        const val REMINDER_SYNC_WORK = "reminder_sync"
        const val EXTRA_EVENT = "extra_event"
        const val EVENT_LIVE_START = "event_live_start"
        const val EVENT_PROGRESS_TICK = "event_progress_tick"
        const val EVENT_DUE = "event_due"
        const val EVENT_EXPIRE = "event_expire"
        private const val LIVE_UPDATE_START_WINDOW_MINUTES = 15
        private const val LIVE_UPDATE_PROGRESS_TICK_MINUTES = 5
    }

    private fun scheduleReminderAlarm(
        payload: DoseLiveUpdatePayload,
        event: String,
        fireAt: LocalDateTime,
        exact: Boolean = true,
    ) {
        val requestCode = "${payload.notificationId}:$event:${fireAt}".hashCode()
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_EVENT, event)
            putDoseLiveUpdatePayload(payload)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val triggerAt = fireAt.toEpochMillis()
        if (exact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else if (exact) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setWindow(AlarmManager.RTC, triggerAt, TimeUnit.MINUTES.toMillis(2), pendingIntent)
        }
    }
}

private fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

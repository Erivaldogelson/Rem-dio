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
        reminderDao.getAll().forEach { reminder ->
            val medication = medicationDao.getMedicationById(reminder.medicationId) ?: return@forEach
            val requestCode = reminder.requestCode()
            val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
                putExtra(ReminderNotifier.EXTRA_MEDICATION_ID, reminder.medicationId)
                putExtra(ReminderNotifier.EXTRA_SCHEDULE_ID, reminder.scheduleId)
                putExtra(ReminderNotifier.EXTRA_NOTIFICATION_ID, requestCode)
                putExtra(EXTRA_MEDICATION_NAME, medication.name)
                putExtra(EXTRA_DOSAGE, medication.dosage)
                putExtra(EXTRA_TRIGGER_AT, reminder.triggerAt.toEpochMillis())
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val triggerAt = reminder.triggerAt.toEpochMillis()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }
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
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_TRIGGER_AT = "extra_trigger_at"
    }
}

private fun ReminderEntity.requestCode(): Int =
    (medicationId.toString() + triggerAt.toString()).hashCode()

private fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

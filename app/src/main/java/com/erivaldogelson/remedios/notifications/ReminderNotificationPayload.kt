package com.erivaldogelson.remedios.notifications

import java.time.LocalDateTime

data class ReminderNotificationPayload(
    val medicationId: Long,
    val scheduleId: Long?,
    val medicationName: String,
    val dosage: String,
    val triggerAt: LocalDateTime,
    val notificationId: Int,
    val promotedOngoing: Boolean,
    val progressPercent: Int = 0,
)


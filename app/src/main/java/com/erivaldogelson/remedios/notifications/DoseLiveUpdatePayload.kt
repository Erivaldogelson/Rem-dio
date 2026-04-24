package com.erivaldogelson.remedios.notifications

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class DoseLiveUpdatePayload(
    val medicationId: Long,
    val scheduleId: Long?,
    val medicationName: String,
    val dosage: String,
    val triggerAt: LocalDateTime,
    val liveUpdateStartAt: LocalDateTime = triggerAt.minusMinutes(15),
    val expiresAt: LocalDateTime = triggerAt.plusMinutes(45),
    val notificationId: Int = notificationId(medicationId, scheduleId, triggerAt),
) {
    fun progressPercent(now: LocalDateTime = LocalDateTime.now()): Int {
        val totalMillis = Duration.between(liveUpdateStartAt, triggerAt).toMillis().coerceAtLeast(1L)
        val elapsedMillis = Duration.between(liveUpdateStartAt, now).toMillis()
        return ((elapsedMillis.coerceIn(0L, totalMillis).toFloat() / totalMillis) * 100)
            .roundToInt()
            .coerceIn(0, 100)
    }

    fun timeLabel(): String = triggerAt.format(DateTimeFormatter.ofPattern("HH:mm"))

    fun chipText(now: LocalDateTime = LocalDateTime.now()): String {
        val minutes = Duration.between(now, triggerAt).toMinutes()
        return when {
            minutes > 0 -> "${minutes.coerceAtMost(99)}m"
            minutes >= -1 -> "agora"
            else -> "ativo"
        }
    }

    companion object {
        fun notificationId(
            medicationId: Long,
            scheduleId: Long?,
            triggerAt: LocalDateTime,
        ): Int = "$medicationId:${scheduleId ?: 0}:${triggerAt}".hashCode()
    }
}

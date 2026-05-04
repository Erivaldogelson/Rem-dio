package com.erivaldogelson.remedios.notifications

import android.content.Intent
import com.erivaldogelson.remedios.domain.model.TreatmentProgressSnapshot
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class TreatmentLiveUpdatePayload(
    val medicationId: Long,
    val medicationName: String,
    val dosage: String,
    val dosesTaken: Int,
    val totalDoses: Int,
    val nextDoseAt: LocalDateTime?,
    val accentColor: Long = 0xFFAA8CFF,
) {
    val progressPercent: Int =
        ((dosesTaken.coerceAtLeast(0) * 100f) / totalDoses.coerceAtLeast(1))
            .toInt()
            .coerceIn(0, 100)

    val isComplete: Boolean = dosesTaken >= totalDoses.coerceAtLeast(1)

    fun nextDoseLabel(): String =
        nextDoseAt?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "sem próxima dose"

    companion object {
        fun from(snapshot: TreatmentProgressSnapshot): TreatmentLiveUpdatePayload =
            TreatmentLiveUpdatePayload(
                medicationId = snapshot.medicationId,
                medicationName = snapshot.medicationName,
                dosage = snapshot.dosage,
                dosesTaken = snapshot.dosesTaken,
                totalDoses = snapshot.totalDoses,
                nextDoseAt = snapshot.nextDoseAt,
                accentColor = snapshot.accentColor,
            )
    }
}

fun Intent.putTreatmentLiveUpdatePayload(payload: TreatmentLiveUpdatePayload): Intent = apply {
    putExtra(DoseLiveService.EXTRA_MEDICATION_ID, payload.medicationId)
    putExtra(DoseLiveService.EXTRA_MEDICATION_NAME, payload.medicationName)
    putExtra(DoseLiveService.EXTRA_DOSAGE, payload.dosage)
    putExtra(DoseLiveService.EXTRA_DOSES_TAKEN, payload.dosesTaken)
    putExtra(DoseLiveService.EXTRA_TOTAL_DOSES, payload.totalDoses)
    putExtra(DoseLiveService.EXTRA_ACCENT_COLOR, payload.accentColor)
    payload.nextDoseAt?.let { putExtra(DoseLiveService.EXTRA_NEXT_DOSE_AT, it.toEpochMillis()) }
}

fun Intent.getTreatmentLiveUpdatePayload(): TreatmentLiveUpdatePayload? {
    val medicationId = getLongExtra(DoseLiveService.EXTRA_MEDICATION_ID, -1L)
    if (medicationId < 0) return null
    val totalDoses = getIntExtra(DoseLiveService.EXTRA_TOTAL_DOSES, 0)
    if (totalDoses <= 0) return null
    val nextDoseMillis = getLongExtra(DoseLiveService.EXTRA_NEXT_DOSE_AT, 0L)
    return TreatmentLiveUpdatePayload(
        medicationId = medicationId,
        medicationName = getStringExtra(DoseLiveService.EXTRA_MEDICATION_NAME).orEmpty(),
        dosage = getStringExtra(DoseLiveService.EXTRA_DOSAGE).orEmpty(),
        dosesTaken = getIntExtra(DoseLiveService.EXTRA_DOSES_TAKEN, 0),
        totalDoses = totalDoses,
        nextDoseAt = nextDoseMillis.takeIf { it > 0L }?.toLocalDateTime(),
        accentColor = getLongExtra(DoseLiveService.EXTRA_ACCENT_COLOR, 0xFFAA8CFF),
    )
}

private fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(this), ZoneId.systemDefault())

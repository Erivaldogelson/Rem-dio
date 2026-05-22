package com.erivaldogelson.remedios.data.repository

import com.erivaldogelson.remedios.data.local.dao.DoseLogDao
import com.erivaldogelson.remedios.data.local.dao.MedicationDao
import com.erivaldogelson.remedios.data.local.dao.ReminderDao
import com.erivaldogelson.remedios.data.local.entity.DoseLogEntity
import com.erivaldogelson.remedios.data.local.entity.DoseScheduleEntity
import com.erivaldogelson.remedios.data.local.entity.MedicationEntity
import com.erivaldogelson.remedios.data.local.entity.MedicationImageEntity
import com.erivaldogelson.remedios.data.local.entity.ReminderEntity
import com.erivaldogelson.remedios.data.local.relation.MedicationWithAssets
import com.erivaldogelson.remedios.domain.model.ActiveReminderSnapshot
import com.erivaldogelson.remedios.domain.model.DashboardSnapshot
import com.erivaldogelson.remedios.domain.model.DoseLogItemModel
import com.erivaldogelson.remedios.domain.model.DoseStatus
import com.erivaldogelson.remedios.domain.model.HistoryFilter
import com.erivaldogelson.remedios.domain.model.ImageSource
import com.erivaldogelson.remedios.domain.model.MedicationDetails
import com.erivaldogelson.remedios.domain.model.MedicationDraft
import com.erivaldogelson.remedios.domain.model.MedicationForm
import com.erivaldogelson.remedios.domain.model.MedicationSummary
import com.erivaldogelson.remedios.domain.model.NextDoseSnapshot
import com.erivaldogelson.remedios.domain.model.ReminderAction
import com.erivaldogelson.remedios.domain.repository.MedicationRepository
import com.erivaldogelson.remedios.notifications.DoseLiveUpdatePayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MedicationRepositoryImpl(
    private val medicationDao: MedicationDao,
    private val doseLogDao: DoseLogDao,
    private val reminderDao: ReminderDao,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val onCancelLiveUpdate: (Int) -> Unit = {},
    private val onMedicationDataChanged: suspend () -> Unit = {},
) : MedicationRepository {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun observeDashboard(): Flow<DashboardSnapshot> = combine(
        medicationDao.observeMedicationCards(),
        doseLogDao.observeLogs(),
        reminderDao.observeActiveReminder(),
    ) { medications, logs, activeReminder ->
        val now = LocalDateTime.now(clock)
        val nextDose = medications
            .mapNotNull { item -> item.toNextDose(now, logs) }
            .minByOrNull { it.scheduledAt }

        val today = now.toLocalDate()
        val dueTodayCount = medications.sumOf { medication ->
            medication.schedules.count {
                medication.medication.startDate <= today &&
                    (medication.medication.endDate == null || medication.medication.endDate >= today)
            }
        }
        val takenTodayCount = logs.count {
            it.scheduledAt.toLocalDate() == today && it.status == DoseStatus.TAKEN
        }

        DashboardSnapshot(
            greetingTitle = "",
            nextDose = nextDose,
            dueTodayCount = dueTodayCount,
            pendingTodayCount = (dueTodayCount - takenTodayCount).coerceAtLeast(0),
            activeReminder = activeReminder?.toSnapshot(medications)
                ?: nextDose?.takeIf { it.remainingMinutes in -45..0 }?.let {
                    ActiveReminderSnapshot(
                        reminderId = -1L,
                        medicationId = it.medicationId,
                        medicationName = it.medicationName,
                        dosage = it.dosage,
                        scheduleId = it.scheduleId,
                        triggerAt = it.scheduledAt,
                        expiresAt = it.scheduledAt.plusMinutes(45),
                        progress = ((45 - kotlin.math.abs(it.remainingMinutes)).coerceIn(0, 45) / 45f),
                    )
                },
        )
    }

    override fun observeMedications(): Flow<List<MedicationSummary>> =
        medicationDao.observeMedicationCards().map { medications ->
            val now = LocalDateTime.now(clock)
            medications.map { medication ->
                val nextDose = medication.toNextDose(now)
                MedicationSummary(
                    id = medication.medication.id,
                    name = medication.medication.name,
                    dosage = medication.medication.dosage,
                    form = medication.medication.form,
                    nextTimeLabel = nextDose?.scheduledAt?.toLocalTime()?.format(timeFormatter).orEmpty(),
                    quantityRemaining = medication.medication.quantityRemaining,
                    accentColor = medication.medication.accentColor,
                    imageUri = medication.images.firstOrNull()?.uri,
                )
            }
        }

    override fun observeMedication(id: Long): Flow<MedicationDetails?> =
        medicationDao.observeMedication(id).map { medication ->
            medication?.let {
                MedicationDetails(
                    id = it.medication.id,
                    name = it.medication.name,
                    dosage = it.medication.dosage,
                    form = it.medication.form,
                    frequencyLabel = it.medication.frequencyLabel,
                    schedules = it.schedules.map(DoseScheduleEntity::time).sorted(),
                    startDate = it.medication.startDate,
                    endDate = it.medication.endDate,
                    instructions = it.medication.instructions,
                    notes = it.medication.notes,
                    quantityRemaining = it.medication.quantityRemaining,
                    accentColor = it.medication.accentColor,
                    iconEmoji = it.medication.iconEmoji,
                    imageUri = it.images.firstOrNull()?.uri,
                    manufacturer = it.medication.manufacturer,
                )
            }
        }

    override suspend fun deleteMedication(id: Long) {
        val remindersToCancel = reminderDao.getByMedicationId(id)
        medicationDao.deleteMedicationWithData(id)
        remindersToCancel.forEach { reminder ->
            val notificationId = DoseLiveUpdatePayload.notificationId(
                medicationId = reminder.medicationId,
                scheduleId = reminder.scheduleId,
                triggerAt = reminder.triggerAt,
            )
            onCancelLiveUpdate(notificationId)
        }
        rescheduleReminders()
        onMedicationDataChanged()
    }

    override suspend fun medicationExists(id: Long): Boolean =
        medicationDao.getMedicationById(id) != null

    override fun observeHistory(filter: HistoryFilter): Flow<List<DoseLogItemModel>> = combine(
        medicationDao.observeMedicationCards(),
        doseLogDao.observeLogs(),
    ) { medications, logs ->
        val now = LocalDateTime.now(clock)
        val cutoff = when (filter) {
            HistoryFilter.DAY -> now.minusDays(1)
            HistoryFilter.WEEK -> now.minusDays(7)
            HistoryFilter.MONTH -> now.minusDays(30)
        }
        val medicationMap = medications.associateBy { it.medication.id }
        logs.filter { it.scheduledAt >= cutoff }
            .map { log ->
                val medication = medicationMap[log.medicationId]?.medication
                DoseLogItemModel(
                    id = log.id,
                    medicationId = log.medicationId,
                    medicationName = medication?.name.orEmpty(),
                    dosage = medication?.dosage ?: "",
                    scheduledAt = log.scheduledAt,
                    actualAt = log.actualAt,
                    status = log.status,
                    note = log.note,
                    accentColor = medication?.accentColor ?: 0xFFAA8CFF,
                )
            }
    }

    override fun observeActiveReminder(): Flow<ActiveReminderSnapshot?> = combine(
        reminderDao.observeActiveReminder(),
        medicationDao.observeMedicationCards(),
    ) { reminder, medications ->
        reminder?.toSnapshot(medications)
    }

    override suspend fun seedIfEmpty() {
        medicationDao.deleteBundledSampleMedicationData()
    }

    override suspend fun upsertMedication(draft: MedicationDraft): Long {
        val now = LocalDateTime.now(clock)
        val existing = draft.id?.let { medicationDao.getMedicationById(it) }
        val entity = MedicationEntity(
            id = draft.id ?: 0L,
            name = draft.name,
            dosage = draft.dosage,
            form = draft.form,
            frequencyLabel = draft.frequencyLabel,
            startDate = draft.startDate,
            endDate = draft.endDate,
            instructions = draft.instructions,
            notes = draft.notes,
            quantityRemaining = draft.quantityRemaining,
            accentColor = draft.accentColor,
            iconEmoji = draft.iconEmoji,
            manufacturer = draft.manufacturer,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        val medicationId = if (existing == null) {
            medicationDao.insertMedication(entity)
        } else {
            medicationDao.updateMedication(entity)
            entity.id
        }

        medicationDao.deleteSchedulesForMedication(medicationId)
        medicationDao.insertSchedules(
            draft.times.sorted().map { time ->
                DoseScheduleEntity(
                    medicationId = medicationId,
                    time = time,
                )
            },
        )

        medicationDao.deleteImagesForMedication(medicationId)
        draft.imageUri?.let { uri ->
            medicationDao.insertImages(
                listOf(
                    MedicationImageEntity(
                        medicationId = medicationId,
                        uri = uri,
                        source = draft.imageSource ?: ImageSource.GALLERY,
                        ocrRawText = draft.ocrSuggestion?.rawText.orEmpty(),
                    ),
                ),
            )
        }

        rescheduleReminders()
        onMedicationDataChanged()
        return medicationId
    }

    override suspend fun recordDoseAction(
        medicationId: Long,
        scheduleId: Long?,
        action: ReminderAction,
        scheduledAt: LocalDateTime,
        actedAt: LocalDateTime,
    ) {
        val medication = medicationDao.getMedicationById(medicationId) ?: return
        val updatedMedication = when (action) {
            ReminderAction.TAKE -> medication.copy(
                quantityRemaining = (medication.quantityRemaining - 1).coerceAtLeast(0),
                updatedAt = actedAt,
            )
            else -> medication.copy(updatedAt = actedAt)
        }
        medicationDao.updateMedication(updatedMedication)

        doseLogDao.insertLog(
            DoseLogEntity(
                medicationId = medicationId,
                scheduleId = scheduleId,
                scheduledAt = scheduledAt,
                actualAt = if (action == ReminderAction.TAKE) actedAt else null,
                status = when (action) {
                    ReminderAction.TAKE -> DoseStatus.TAKEN
                    ReminderAction.SNOOZE -> DoseStatus.SNOOZED
                    ReminderAction.SKIP -> DoseStatus.SKIPPED
                },
                note = "",
            ),
        )

        reminderDao.deactivateActiveReminders()
        if (action == ReminderAction.SNOOZE) {
            val snoozedAt = if (scheduledAt.isAfter(actedAt)) {
                scheduledAt.plusMinutes(15)
            } else {
                actedAt.plusMinutes(15)
            }
            val snoozedReminder = ReminderEntity(
                medicationId = medicationId,
                scheduleId = scheduleId,
                triggerAt = snoozedAt,
                expiresAt = snoozedAt.plusMinutes(45),
                isActive = false,
                action = ReminderAction.SNOOZE,
            )
            val reminders = buildReminderEntities(now = actedAt, extraReminder = snoozedReminder)
            reminderDao.clearAll()
            reminderDao.insertAll(reminders)
        } else {
            rescheduleReminders()
        }
        onMedicationDataChanged()
    }

    override suspend fun activateReminder(
        medicationId: Long,
        scheduleId: Long?,
        triggerAt: LocalDateTime,
    ) {
        reminderDao.activateReminder(medicationId, scheduleId, triggerAt)
        onMedicationDataChanged()
    }

    override suspend fun expireReminder(
        medicationId: Long,
        scheduleId: Long?,
        triggerAt: LocalDateTime,
    ) {
        reminderDao.deactivateReminder(medicationId, scheduleId, triggerAt)
        rescheduleReminders()
        onMedicationDataChanged()
    }

    override suspend fun rescheduleReminders() {
        reminderDao.clearAll()
        reminderDao.insertAll(buildReminderEntities(LocalDateTime.now(clock)))
    }

    private suspend fun buildReminderEntities(
        now: LocalDateTime,
        extraReminder: ReminderEntity? = null,
    ): List<ReminderEntity> {
        val reminders = medicationDao.getMedicationCards()
            .flatMap { medication ->
                medication.schedules.flatMap { schedule ->
                    listOf(now.toLocalDate(), now.toLocalDate().plusDays(1))
                        .map { date -> LocalDateTime.of(date, schedule.time) }
                        .filter { candidate ->
                            candidate >= now.minusMinutes(1) &&
                                dateWithinMedicationRange(candidate.toLocalDate(), medication.medication)
                        }
                        .take(1)
                        .map { triggerAt ->
                            ReminderEntity(
                                medicationId = medication.medication.id,
                                scheduleId = schedule.id.takeIf { it != 0L },
                                triggerAt = triggerAt,
                                expiresAt = triggerAt.plusMinutes(45),
                                isActive = false,
                            )
                        }
                }
            }
            .sortedBy(ReminderEntity::triggerAt)
            .take(18)
            .toMutableList()

        extraReminder?.let(reminders::add)
        return reminders.sortedBy(ReminderEntity::triggerAt)
    }

    private fun dateWithinMedicationRange(date: LocalDate, medication: MedicationEntity): Boolean {
        val starts = !date.isBefore(medication.startDate)
        val ends = medication.endDate?.let { !date.isAfter(it) } ?: true
        return starts && ends
    }

    private fun MedicationWithAssets.toNextDose(
        now: LocalDateTime,
        doseLogs: List<DoseLogEntity> = emptyList(),
    ): NextDoseSnapshot? {
        val completedOccurrences = doseLogs
            .filter { log ->
                log.medicationId == medication.id &&
                    log.status in setOf(DoseStatus.TAKEN, DoseStatus.SKIPPED)
            }
            .map { log -> (log.scheduleId ?: 0L) to log.scheduledAt }
            .toSet()

        val nextSchedule = schedules
            .mapNotNull { schedule ->
                nextOccurrenceFor(schedule.time, medication, now)?.let { nextTime ->
                    schedule to nextTime
                }
            }
            .filterNot { (schedule, scheduledAt) ->
                val occurrenceKey = (schedule.id.takeIf { it != 0L } ?: 0L) to scheduledAt
                occurrenceKey in completedOccurrences
            }
            .minByOrNull { (_, scheduledAt) -> scheduledAt }
            ?: return null

        val remainingMinutes = java.time.Duration.between(now, nextSchedule.second).toMinutes()
        return NextDoseSnapshot(
            medicationId = medication.id,
            medicationName = medication.name,
            dosage = medication.dosage,
            scheduledAt = nextSchedule.second,
            remainingMinutes = remainingMinutes,
            progress = ((360 - remainingMinutes.coerceIn(0, 360)) / 360f),
            imageUri = images.firstOrNull()?.uri,
            accentColor = medication.accentColor,
            scheduleId = nextSchedule.first.id.takeIf { it != 0L },
        )
    }

    private fun nextOccurrenceFor(
        time: LocalTime,
        medication: MedicationEntity,
        now: LocalDateTime,
    ): LocalDateTime? {
        val today = now.toLocalDate()
        val todayCandidate = LocalDateTime.of(today, time)
        val tomorrowCandidate = LocalDateTime.of(today.plusDays(1), time)
        return when {
            dateWithinMedicationRange(today, medication) && todayCandidate >= now.minusMinutes(1) -> todayCandidate
            dateWithinMedicationRange(today.plusDays(1), medication) -> tomorrowCandidate
            else -> null
        }
    }

    private fun ReminderEntity.toSnapshot(
        medications: List<MedicationWithAssets>,
    ): ActiveReminderSnapshot? {
        val medication = medications.firstOrNull { it.medication.id == medicationId }?.medication ?: return null
        val totalWindowMinutes = java.time.Duration.between(triggerAt, expiresAt).toMinutes().coerceAtLeast(1)
        val elapsedMinutes = java.time.Duration.between(triggerAt, LocalDateTime.now(clock)).toMinutes()
        return ActiveReminderSnapshot(
            reminderId = id,
            medicationId = medicationId,
            medicationName = medication.name,
            dosage = medication.dosage,
            scheduleId = scheduleId,
            triggerAt = triggerAt,
            expiresAt = expiresAt,
            progress = (elapsedMinutes.coerceIn(0, totalWindowMinutes) / totalWindowMinutes.toFloat()),
        )
    }
}

package com.erivaldogelson.remedios.data.local

import androidx.room.TypeConverter
import com.erivaldogelson.remedios.domain.model.DoseStatus
import com.erivaldogelson.remedios.domain.model.ImageSource
import com.erivaldogelson.remedios.domain.model.MedicationForm
import com.erivaldogelson.remedios.domain.model.ReminderAction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class RoomConverters {
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)

    @TypeConverter
    fun localTimeToString(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter
    fun medicationFormToString(value: MedicationForm?): String? = value?.name

    @TypeConverter
    fun stringToMedicationForm(value: String?): MedicationForm? = value?.let(MedicationForm::valueOf)

    @TypeConverter
    fun doseStatusToString(value: DoseStatus?): String? = value?.name

    @TypeConverter
    fun stringToDoseStatus(value: String?): DoseStatus? = value?.let(DoseStatus::valueOf)

    @TypeConverter
    fun reminderActionToString(value: ReminderAction?): String? = value?.name

    @TypeConverter
    fun stringToReminderAction(value: String?): ReminderAction? = value?.let(ReminderAction::valueOf)

    @TypeConverter
    fun imageSourceToString(value: ImageSource?): String? = value?.name

    @TypeConverter
    fun stringToImageSource(value: String?): ImageSource? = value?.let(ImageSource::valueOf)
}


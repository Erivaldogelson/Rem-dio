package com.erivaldogelson.remedios.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.erivaldogelson.remedios.data.local.dao.DoseLogDao
import com.erivaldogelson.remedios.data.local.dao.MedicationDao
import com.erivaldogelson.remedios.data.local.dao.ReminderDao
import com.erivaldogelson.remedios.data.local.dao.SettingsDao
import com.erivaldogelson.remedios.data.local.entity.AppSettingsEntity
import com.erivaldogelson.remedios.data.local.entity.DoseLogEntity
import com.erivaldogelson.remedios.data.local.entity.DoseScheduleEntity
import com.erivaldogelson.remedios.data.local.entity.MedicationEntity
import com.erivaldogelson.remedios.data.local.entity.MedicationImageEntity
import com.erivaldogelson.remedios.data.local.entity.ReminderEntity

@Database(
    entities = [
        MedicationEntity::class,
        DoseScheduleEntity::class,
        DoseLogEntity::class,
        ReminderEntity::class,
        MedicationImageEntity::class,
        AppSettingsEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun doseLogDao(): DoseLogDao
    abstract fun reminderDao(): ReminderDao
    abstract fun settingsDao(): SettingsDao
}


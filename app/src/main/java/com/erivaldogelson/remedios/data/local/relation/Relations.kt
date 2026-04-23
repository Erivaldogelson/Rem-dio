package com.erivaldogelson.remedios.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.erivaldogelson.remedios.data.local.entity.DoseScheduleEntity
import com.erivaldogelson.remedios.data.local.entity.MedicationEntity
import com.erivaldogelson.remedios.data.local.entity.MedicationImageEntity

data class MedicationWithAssets(
    @Embedded val medication: MedicationEntity,
    @Relation(parentColumn = "id", entityColumn = "medicationId")
    val schedules: List<DoseScheduleEntity>,
    @Relation(parentColumn = "id", entityColumn = "medicationId")
    val images: List<MedicationImageEntity>,
)


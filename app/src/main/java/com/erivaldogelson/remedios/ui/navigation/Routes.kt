package com.erivaldogelson.remedios.ui.navigation

object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Today = "today"
    const val Medications = "medications"
    const val AddMedication = "add_medication"
    const val ScanMedication = "scan_medication"
    const val History = "history"
    const val Settings = "settings"
    const val Permissions = "permissions"
    const val ActiveReminder = "active_reminder"
    const val MedicationDetail = "medication_detail/{medicationId}"
    const val EditMedication = "edit_medication/{medicationId}"

    fun medicationDetail(id: Long) = "medication_detail/$id"
    fun editMedication(id: Long) = "edit_medication/$id"
}

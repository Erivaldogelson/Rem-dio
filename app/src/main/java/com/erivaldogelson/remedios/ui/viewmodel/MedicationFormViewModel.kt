package com.erivaldogelson.remedios.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erivaldogelson.remedios.domain.model.ImageSource
import com.erivaldogelson.remedios.domain.model.MedicationDetails
import com.erivaldogelson.remedios.domain.model.MedicationDraft
import com.erivaldogelson.remedios.domain.model.MedicationForm
import com.erivaldogelson.remedios.domain.model.OcrSuggestion
import com.erivaldogelson.remedios.domain.repository.MedicationRepository
import com.erivaldogelson.remedios.media.MedicationImageManager
import com.erivaldogelson.remedios.ocr.MedicationTextRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class MedicationFormUiState(
    val draft: MedicationDraft = MedicationDraft(),
    val timesText: String = MedicationDraft().times.joinToString(", ") {
        it.format(DateTimeFormatter.ofPattern("HH:mm"))
    },
    val isAnalyzingImage: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedMedicationId: Long? = null,
)

class MedicationFormViewModel(
    private val medicationRepository: MedicationRepository,
    private val imageManager: MedicationImageManager,
    private val textRecognizer: MedicationTextRecognizer,
    medicationId: Long? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MedicationFormUiState())
    val uiState = _uiState.asStateFlow()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    init {
        medicationId?.let { id ->
            viewModelScope.launch {
                val details = medicationRepository.observeMedication(id).filterNotNull().first()
                val draft = details.toDraft()
                _uiState.update {
                    it.copy(
                        draft = draft,
                        timesText = draft.times.joinToString(", ") { time -> time.format(timeFormatter) },
                    )
                }
            }
        }
    }

    fun updateName(value: String) = updateDraft { copy(name = value) }
    fun updateDosage(value: String) = updateDraft { copy(dosage = value) }
    fun updateFrequency(value: String) = updateDraft { copy(frequencyLabel = value) }
    fun updateInstructions(value: String) = updateDraft { copy(instructions = value) }
    fun updateNotes(value: String) = updateDraft { copy(notes = value) }
    fun updateManufacturer(value: String) = updateDraft { copy(manufacturer = value) }
    fun updateQuantity(value: String) = updateDraft { copy(quantityRemaining = value.toIntOrNull() ?: quantityRemaining) }
    fun updateStartDate(value: LocalDate) = updateDraft { copy(startDate = value) }
    fun updateEndDate(value: LocalDate?) = updateDraft { copy(endDate = value) }
    fun updateForm(value: MedicationForm) = updateDraft { copy(form = value) }
    fun updateAccentColor(value: Long) = updateDraft { copy(accentColor = value) }
    fun updateTimesText(value: String) {
        val parsedTimes = parseTimes(value)
        _uiState.update { state ->
            state.copy(
                timesText = value,
                draft = if (parsedTimes.isNotEmpty()) {
                    state.draft.copy(times = parsedTimes)
                } else {
                    state.draft
                },
            )
        }
    }

    fun applyOcrSuggestion(suggestion: OcrSuggestion) {
        updateDraft {
            copy(
                name = suggestion.suggestedName.ifBlank { name },
                dosage = suggestion.suggestedDosage.ifBlank { dosage },
                manufacturer = suggestion.suggestedManufacturer.ifBlank { manufacturer },
                instructions = suggestion.suggestedInstructions.ifBlank { instructions },
                ocrSuggestion = suggestion,
            )
        }
    }

    fun onImageSelected(uri: Uri, source: ImageSource) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingImage = true, errorMessage = null) }
            runCatching {
                val persistedUri = imageManager.persistUri(uri)
                val suggestion = textRecognizer.recognizeFromUri(uri)
                _uiState.update { state ->
                    state.copy(
                        draft = state.draft.copy(
                            imageUri = persistedUri,
                            imageSource = source,
                            ocrSuggestion = suggestion,
                            name = suggestion.suggestedName.ifBlank { state.draft.name },
                            dosage = suggestion.suggestedDosage.ifBlank { state.draft.dosage },
                            manufacturer = suggestion.suggestedManufacturer.ifBlank { state.draft.manufacturer },
                        ),
                        isAnalyzingImage = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAnalyzingImage = false,
                        errorMessage = error.message,
                    )
                }
            }
        }
    }

    fun clearImage() = updateDraft { copy(imageUri = null, imageSource = null, ocrSuggestion = null) }

    fun saveMedication() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                medicationRepository.upsertMedication(_uiState.value.draft)
            }.onSuccess { medicationId ->
                _uiState.update { it.copy(isSaving = false, savedMedicationId = medicationId) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message,
                    )
                }
            }
        }
    }

    fun consumeSavedState() {
        _uiState.update { it.copy(savedMedicationId = null) }
    }

    private fun updateDraft(transform: MedicationDraft.() -> MedicationDraft) {
        _uiState.update { state -> state.copy(draft = state.draft.transform()) }
    }

    private fun parseTimes(value: String): List<LocalTime> =
        value.split(",")
            .map(String::trim)
            .mapNotNull { time -> runCatching { LocalTime.parse(time, timeFormatter) }.getOrNull() }
            .distinct()
            .sorted()

    private fun MedicationDetails.toDraft(): MedicationDraft =
        MedicationDraft(
            id = id,
            name = name,
            dosage = dosage,
            form = form,
            frequencyLabel = frequencyLabel,
            times = schedules,
            startDate = startDate,
            endDate = endDate,
            instructions = instructions,
            notes = notes,
            quantityRemaining = quantityRemaining,
            accentColor = accentColor,
            iconEmoji = iconEmoji,
            imageUri = imageUri,
            manufacturer = manufacturer,
        )
}

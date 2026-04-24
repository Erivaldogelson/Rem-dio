@file:OptIn(ExperimentalMaterial3Api::class)

package com.erivaldogelson.remedios.ui.screens

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreviewUseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.erivaldogelson.remedios.domain.model.MedicationDetails
import com.erivaldogelson.remedios.domain.model.MedicationForm
import com.erivaldogelson.remedios.domain.model.MedicationSummary
import com.erivaldogelson.remedios.domain.model.OcrSuggestion
import com.erivaldogelson.remedios.ocr.MedicationScanAnalyzer
import com.erivaldogelson.remedios.ui.components.AnimatedPrimaryActionButton
import com.erivaldogelson.remedios.ui.components.EmptyStateCard
import com.erivaldogelson.remedios.ui.components.MedicationCard
import com.erivaldogelson.remedios.ui.components.OcrResultConfirmationCard
import com.erivaldogelson.remedios.ui.components.PremiumScaffoldBackground
import com.erivaldogelson.remedios.ui.theme.InkCard
import com.erivaldogelson.remedios.ui.theme.Mist
import com.erivaldogelson.remedios.ui.theme.MistMuted
import com.erivaldogelson.remedios.ui.theme.RemediosTheme
import com.erivaldogelson.remedios.ui.theme.SoftLilac
import com.erivaldogelson.remedios.ui.viewmodel.MedicationFormUiState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun MedicationListScreen(
    medications: List<MedicationSummary>,
    onMedicationClick: (MedicationSummary) -> Unit,
    onEditMedication: (MedicationSummary) -> Unit,
    onDeleteMedication: (MedicationSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("Medicamentos", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(
                "Seu arsenal diário com imagem, dose, horários e histórico.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (medications.isEmpty()) {
                EmptyStateCard(
                    title = "Nenhum remédio cadastrado",
                    message = "Adicione o primeiro medicamento para começar a organizar doses, fotos e lembretes.",
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(medications) { medication ->
                        MedicationCard(
                            medication = medication,
                            onClick = { onMedicationClick(medication) },
                            onEdit = { onEditMedication(medication) },
                            onDelete = { onDeleteMedication(medication) },
                        )
                    }
                    item { Spacer(Modifier.height(90.dp)) }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun AddMedicationScreen(
    uiState: MedicationFormUiState,
    isEditing: Boolean = false,
    onNameChange: (String) -> Unit,
    onDosageChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onTimesChange: (String) -> Unit,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate?) -> Unit,
    onInstructionsChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onManufacturerChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onFormChange: (MedicationForm) -> Unit,
    onPickImage: () -> Unit,
    onCaptureImage: () -> Unit,
    onOpenScan: () -> Unit,
    onApplySuggestion: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = uiState.draft

    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isEditing) "Editar medicamento" else "Adicionar medicamento") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    "Cadastre com visual, contexto e horários claros.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ImageSelectorCard(
                    imageUri = draft.imageUri,
                    isAnalyzing = uiState.isAnalyzingImage,
                    onPickImage = onPickImage,
                    onCaptureImage = onCaptureImage,
                    onScan = onOpenScan,
                )
                if (draft.ocrSuggestion?.rawText?.isNotBlank() == true) {
                    OcrResultConfirmationCard(
                        suggestion = draft.ocrSuggestion,
                        onApply = onApplySuggestion,
                    )
                }
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nome do remédio") },
                    leadingIcon = { Icon(Icons.Rounded.Medication, contentDescription = null) },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = draft.dosage,
                        onValueChange = onDosageChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Dose") },
                    )
                    OutlinedTextField(
                        value = draft.quantityRemaining.toString(),
                        onValueChange = onQuantityChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Qtde restante") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MedicationForm.entries.forEach { form ->
                        FilterChip(
                            selected = draft.form == form,
                            onClick = { onFormChange(form) },
                            label = { Text(form.name.lowercase().replaceFirstChar(Char::titlecase)) },
                        )
                    }
                }
                OutlinedTextField(
                    value = draft.frequencyLabel,
                    onValueChange = onFrequencyChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Frequência") },
                )
                OutlinedTextField(
                    value = uiState.timesText,
                    onValueChange = onTimesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Horários (ex: 08:00, 20:00)") },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DatePickerField(
                        label = "Início",
                        date = draft.startDate,
                        onDateChange = { selectedDate -> selectedDate?.let(onStartDateChange) },
                        modifier = Modifier.weight(1f),
                    )
                    DatePickerField(
                        label = "Fim",
                        date = draft.endDate,
                        onDateChange = onEndDateChange,
                        allowClear = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = draft.manufacturer,
                    onValueChange = onManufacturerChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Laboratório / fabricante") },
                )
                OutlinedTextField(
                    value = draft.instructions,
                    onValueChange = onInstructionsChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Instruções") },
                )
                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("Observações") },
                )
                uiState.errorMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                AnimatedPrimaryActionButton(
                    text = if (uiState.isSaving) {
                        "Salvando..."
                    } else if (isEditing) {
                        "Atualizar medicamento"
                    } else {
                        "Salvar medicamento"
                    },
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.EditNote,
                )
                Spacer(Modifier.height(90.dp))
            }
        }
    }
}

@Composable
private fun DatePickerField(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    allowClear: Boolean = false,
) {
    var showPicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    OutlinedTextField(
        value = date?.format(dateFormatter) ?: "Sem fim",
        onValueChange = {},
        modifier = modifier,
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            TextButton(onClick = { showPicker = true }) {
                Text("Escolher")
            }
        },
    )

    if (showPicker) {
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = date?.toUtcMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { selectedMillis ->
                            onDateChange(selectedMillis.toLocalDateUtc())
                        }
                        showPicker = false
                    },
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                Row {
                    if (allowClear) {
                        TextButton(
                            onClick = {
                                onDateChange(null)
                                showPicker = false
                            },
                        ) {
                            Text("Sem fim")
                        }
                    }
                    TextButton(onClick = { showPicker = false }) {
                        Text("Cancelar")
                    }
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun ImageSelectorCard(
    imageUri: String?,
    isAnalyzing: Boolean,
    onPickImage: () -> Unit,
    onCaptureImage: () -> Unit,
    onScan: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Foto do remédio", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Imagem do remédio",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(SoftLilac.copy(alpha = 0.08f), MaterialTheme.shapes.large),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Sem foto ainda", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (isAnalyzing) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Analisando imagem e OCR...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onCaptureImage, label = { Text("Câmera") }, leadingIcon = {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                })
                AssistChip(onClick = onPickImage, label = { Text("Galeria") }, leadingIcon = {
                    Icon(Icons.Rounded.Image, contentDescription = null)
                })
                AssistChip(onClick = onScan, label = { Text("Escanear") }, leadingIcon = {
                    Icon(Icons.Rounded.QrCodeScanner, contentDescription = null)
                })
            }
        }
    }
}

@Composable
fun MedicationDetailScreen(
    details: MedicationDetails?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(details?.name ?: "Detalhes") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                details?.let { medication ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(medication.name, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text("${medication.dosage} • ${medication.frequencyLabel}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Horários: ${medication.schedules.joinToString { it.format(DateTimeFormatter.ofPattern("HH:mm")) }}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Instruções: ${medication.instructions}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Observações: ${medication.notes}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Quantidade restante: ${medication.quantityRemaining}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } ?: EmptyStateCard(
                    title = "Detalhes indisponíveis",
                    message = "Não foi possível carregar este medicamento.",
                )
            }
        }
    }
}

@Composable
fun ScanMedicationScreen(
    onConfirmSuggestion: (OcrSuggestion) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var suggestion by remember { mutableStateOf(OcrSuggestion()) }

    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            CameraPreviewBox(
                onSuggestion = { latest ->
                    if (latest.rawText.isNotBlank()) suggestion = latest
                },
            )
            TopAppBar(
                title = { Text("Escanear remédio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Leitura em tempo real", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        suggestion.suggestedName.ifBlank { "Aponte a câmera para a embalagem" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (suggestion.suggestedDosage.isNotBlank()) {
                        Text("Dose detectada: ${suggestion.suggestedDosage}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    AnimatedPrimaryActionButton(
                        text = "Usar esta leitura",
                        onClick = { onConfirmSuggestion(suggestion) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.QrCodeScanner,
                    )
                }
            }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun CameraPreviewBox(
    onSuggestion: (OcrSuggestion) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestSuggestion by rememberUpdatedState(onSuggestion)
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val analyzerExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val analyzer = MedicationScanAnalyzer { latestSuggestion(it) }
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = CameraPreviewUseCase.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(analyzerExecutor, analyzer) }
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis,
            )
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
            analyzerExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize(),
    )
}

fun parseTimesInput(input: String): List<LocalTime> =
    input.split(",")
        .map(String::trim)
        .mapNotNull { value ->
            runCatching { LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm")) }.getOrNull()
        }
        .distinct()
        .sorted()

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Preview
@Composable
private fun MedicationListPreview() {
    RemediosTheme {
        MedicationListScreen(
            medications = PreviewData.medicationList,
            onMedicationClick = { _ -> },
            onEditMedication = { _ -> },
            onDeleteMedication = { _ -> },
        )
    }
}

@Preview
@Composable
private fun AddMedicationPreview() {
    RemediosTheme {
        AddMedicationScreen(
            uiState = MedicationFormUiState(draft = PreviewData.draft.copy(ocrSuggestion = PreviewData.ocrSuggestion)),
            onNameChange = {},
            onDosageChange = {},
            onFrequencyChange = {},
            onTimesChange = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onInstructionsChange = {},
            onNotesChange = {},
            onManufacturerChange = {},
            onQuantityChange = {},
            onFormChange = { _ -> },
            onPickImage = {},
            onCaptureImage = {},
            onOpenScan = {},
            onApplySuggestion = {},
            onSave = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun MedicationDetailPreview() {
    RemediosTheme {
        MedicationDetailScreen(
            details = PreviewData.medicationDetails,
            onBack = {},
        )
    }
}

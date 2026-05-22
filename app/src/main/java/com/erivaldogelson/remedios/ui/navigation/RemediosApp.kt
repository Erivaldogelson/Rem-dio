package com.erivaldogelson.remedios.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.erivaldogelson.remedios.core.AppContainer
import com.erivaldogelson.remedios.core.AppViewModelFactory
import com.erivaldogelson.remedios.domain.model.ImageSource
import com.erivaldogelson.remedios.domain.model.OcrSuggestion
import com.erivaldogelson.remedios.domain.model.SettingsSnapshot
import com.erivaldogelson.remedios.ui.components.BottomBarItem
import com.erivaldogelson.remedios.ui.components.PillBottomNavigation
import com.erivaldogelson.remedios.ui.screens.ActiveReminderScreen
import com.erivaldogelson.remedios.ui.screens.AddMedicationScreen
import com.erivaldogelson.remedios.ui.screens.DashboardScreen
import com.erivaldogelson.remedios.ui.screens.HistoryScreen
import com.erivaldogelson.remedios.ui.screens.MedicationDetailScreen
import com.erivaldogelson.remedios.ui.screens.MedicationListScreen
import com.erivaldogelson.remedios.ui.screens.OnboardingScreen
import com.erivaldogelson.remedios.ui.screens.PermissionsScreen
import com.erivaldogelson.remedios.ui.screens.ScanMedicationScreen
import com.erivaldogelson.remedios.ui.screens.SettingsScreen
import com.erivaldogelson.remedios.ui.screens.SplashScreen
import com.erivaldogelson.remedios.ui.theme.RemediosTheme
import com.erivaldogelson.remedios.ui.viewmodel.DashboardViewModel
import com.erivaldogelson.remedios.ui.viewmodel.HistoryViewModel
import com.erivaldogelson.remedios.ui.viewmodel.MedicationDetailViewModel
import com.erivaldogelson.remedios.ui.viewmodel.MedicationFormViewModel
import com.erivaldogelson.remedios.ui.viewmodel.MedicationListViewModel
import com.erivaldogelson.remedios.ui.viewmodel.OnboardingViewModel
import com.erivaldogelson.remedios.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.first

@Composable
fun RemediosApp(
    container: AppContainer,
) {
    val loadedSettings by container.settingsRepository.settings.collectAsStateWithLifecycle(
        initialValue = null,
    )
    val settings = loadedSettings ?: SettingsSnapshot()
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    val labels = navLabels(settings.languageTag)
    val bottomItems = listOf(
        BottomBarItem(Routes.Today, labels.today, Icons.Rounded.Home),
        BottomBarItem(Routes.History, labels.history, Icons.Rounded.BarChart),
        BottomBarItem(Routes.Settings, labels.settings, Icons.Rounded.Settings),
    )
    val addItem = BottomBarItem(Routes.AddMedication, labels.newItem, Icons.Rounded.Add)

    RemediosTheme(settings = settings) {
        Scaffold(
            bottomBar = {
                if (currentRoute in setOf(Routes.Today, Routes.Medications, Routes.AddMedication, Routes.History, Routes.Settings)) {
                    PillBottomNavigation(
                        items = bottomItems,
                        addItem = addItem,
                        selectedRoute = currentRoute.orEmpty(),
                        transparency = settings.navigationPillTransparency,
                        onSelect = { item ->
                            if (item.route == Routes.Today) {
                                val popped = navController.popBackStack(Routes.Today, inclusive = false)
                                if (!popped) {
                                    navController.navigate(Routes.Today) {
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.Today) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = item.route != Routes.AddMedication
                                }
                            }
                        },
                    )
                }
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.Splash,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(PageTransitionMillis),
                        ) + fadeIn(animationSpec = tween(PageTransitionMillis / 2))
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(PageTransitionMillis),
                        ) + fadeOut(animationSpec = tween(PageTransitionMillis / 2))
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(PageTransitionMillis),
                        ) + fadeIn(animationSpec = tween(PageTransitionMillis / 2))
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(PageTransitionMillis),
                        ) + fadeOut(animationSpec = tween(PageTransitionMillis / 2))
                    },
                ) {
                    composable(Routes.Splash) {
                        SplashScreen(
                            settingsReady = loadedSettings != null,
                            onFinished = {
                                navController.navigate(
                                    if (loadedSettings?.onboardingCompleted == true) Routes.Today else Routes.Onboarding,
                                ) {
                                    popUpTo(Routes.Splash) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(Routes.Onboarding) {
                        val viewModel: OnboardingViewModel = viewModel(
                            factory = AppViewModelFactory { OnboardingViewModel(container.settingsRepository) },
                        )
                        OnboardingScreen(
                            onContinue = {
                                viewModel.completeOnboarding {
                                    navController.navigate(Routes.Today) {
                                        popUpTo(Routes.Onboarding) { inclusive = true }
                                    }
                                }
                            },
                        )
                    }
                    composable(Routes.Today) {
                        val viewModel: DashboardViewModel = viewModel(
                            factory = AppViewModelFactory {
                                DashboardViewModel(
                                    medicationRepository = container.medicationRepository,
                                    reminderScheduler = container.reminderScheduler,
                                    liveUpdateManager = container.liveUpdateManager,
                                )
                            },
                        )
                        val state by viewModel.state.collectAsStateWithLifecycle()
                        DashboardScreen(
                            state = state,
                            onTakeNow = { viewModel.takeNow(state) },
                            onSnooze = { viewModel.snooze(state) },
                            onSkip = { viewModel.skip(state) },
                            onOpenMedications = { navController.navigate(Routes.Medications) },
                            onOpenActiveReminder = { navController.navigate(Routes.ActiveReminder) },
                        )
                    }
                    composable(Routes.Medications) {
                        val viewModel: MedicationListViewModel = viewModel(
                            factory = AppViewModelFactory { MedicationListViewModel(container.medicationRepository) },
                        )
                        val medications by viewModel.medications.collectAsStateWithLifecycle()
                        MedicationListScreen(
                            medications = medications,
                            onMedicationClick = { medication ->
                                navController.navigate(Routes.medicationDetail(medication.id))
                            },
                            onEditMedication = { medication ->
                                navController.navigate(Routes.editMedication(medication.id))
                            },
                            onDeleteMedication = { medication -> viewModel.deleteMedication(medication.id) },
                        )
                    }
                    composable(Routes.AddMedication) { backStackEntry ->
                        AddMedicationRoute(
                            container = container,
                            navController = navController,
                            backStackEntry = backStackEntry,
                        )
                    }
                    composable(
                        route = Routes.EditMedication,
                        arguments = listOf(navArgument("medicationId") { type = NavType.LongType }),
                    ) { backStackEntry ->
                        AddMedicationRoute(
                            container = container,
                            navController = navController,
                            backStackEntry = backStackEntry,
                            medicationId = backStackEntry.arguments?.getLong("medicationId"),
                        )
                    }
                    composable(Routes.ScanMedication) {
                        ScanMedicationScreen(
                            onConfirmSuggestion = { suggestion ->
                                navController.previousBackStackEntry?.savedStateHandle?.apply {
                                    set("ocr_name", suggestion.suggestedName)
                                    set("ocr_dose", suggestion.suggestedDosage)
                                    set("ocr_manufacturer", suggestion.suggestedManufacturer)
                                    set("ocr_instructions", suggestion.suggestedInstructions)
                                    set("ocr_raw", suggestion.rawText)
                                }
                                navController.popBackStack()
                            },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(
                        route = Routes.MedicationDetail,
                        arguments = listOf(navArgument("medicationId") { type = NavType.LongType }),
                    ) { backStackEntry ->
                        val medicationId = backStackEntry.arguments?.getLong("medicationId") ?: 0L
                        val viewModel: MedicationDetailViewModel = viewModel(
                            factory = AppViewModelFactory {
                                MedicationDetailViewModel(medicationId, container.medicationRepository)
                            },
                        )
                        val detail by viewModel.detail.collectAsStateWithLifecycle()
                        MedicationDetailScreen(
                            details = detail,
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(Routes.History) {
                        val viewModel: HistoryViewModel = viewModel(
                            factory = AppViewModelFactory { HistoryViewModel(container.medicationRepository) },
                        )
                        val items by viewModel.items.collectAsStateWithLifecycle()
                        val filter by viewModel.selectedFilter.collectAsStateWithLifecycle()
                        HistoryScreen(
                            items = items,
                            selectedFilter = filter,
                            onSelectFilter = viewModel::selectFilter,
                        )
                    }
                    composable(Routes.Settings) {
                        val viewModel: SettingsViewModel = viewModel(
                            factory = AppViewModelFactory {
                                SettingsViewModel(
                                    settingsRepository = container.settingsRepository,
                                )
                            },
                        )
                        val state by viewModel.settings.collectAsStateWithLifecycle()
                        SettingsScreen(
                            settings = state,
                            onThemeModeChange = viewModel::setThemeMode,
                            onDynamicColorChange = viewModel::setDynamicColor,
                            onLiveUpdatesChange = viewModel::setLiveUpdates,
                            onHapticsChange = viewModel::setHaptics,
                            onLanguageChange = viewModel::setLanguageTag,
                            onNowBarColorChange = viewModel::setNowBarColor,
                            onNowBarToneChange = viewModel::setNowBarTone,
                            onNavigationPillTransparencyChange = viewModel::setNavigationPillTransparency,
                            onOpenPermissions = { navController.navigate(Routes.Permissions) },
                        )
                    }
                    composable(Routes.Permissions) {
                        PermissionsRoute(
                            navController = navController,
                        )
                    }
                    composable(Routes.ActiveReminder) {
                        val viewModel: DashboardViewModel = viewModel(
                            factory = AppViewModelFactory {
                                DashboardViewModel(
                                    medicationRepository = container.medicationRepository,
                                    reminderScheduler = container.reminderScheduler,
                                    liveUpdateManager = container.liveUpdateManager,
                                )
                            },
                        )
                        val state by viewModel.state.collectAsStateWithLifecycle()
                        ActiveReminderScreen(
                            activeReminder = state.activeReminder,
                            onTakeNow = { viewModel.takeNow(state) },
                            onSnooze = { viewModel.snooze(state) },
                            onSkip = { viewModel.skip(state) },
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}

private data class NavLabels(
    val today: String,
    val medications: String,
    val newItem: String,
    val history: String,
    val settings: String,
)

private fun navLabels(languageTag: String): NavLabels {
    val locale = resolvedLocale(languageTag)
    return when {
        locale.language == "en" -> NavLabels("Today", "Meds", "New", "History", "Settings")
        locale.language == "pt" && locale.country == "PT" -> NavLabels("Hoje", "Remédios", "Novo", "Histórico", "Defin.")
        locale.language == "pt" && locale.country == "AO" -> NavLabels("Hoje", "Remédios", "Novo", "Histórico", "Defin.")
        locale.language == "es" -> NavLabels("Hoy", "Medic.", "Nuevo", "Historial", "Ajustes")
        locale.language == "fr" -> NavLabels("Auj.", "Médic.", "Nouv.", "Hist.", "Régl.")
        locale.language == "zh" -> NavLabels("今天", "药品", "新增", "记录", "设置")
        locale.language == "ja" -> NavLabels("今日", "薬", "追加", "履歴", "設定")
        else -> NavLabels("Hoje", "Remédios", "Novo", "Histórico", "Config.")
    }
}

private fun resolvedLocale(languageTag: String): java.util.Locale =
    if (languageTag == "system") {
        java.util.Locale.getDefault()
    } else {
        java.util.Locale.forLanguageTag(languageTag)
    }

private const val PageTransitionMillis = 280

@Composable
private fun AddMedicationRoute(
    container: AppContainer,
    navController: androidx.navigation.NavHostController,
    backStackEntry: NavBackStackEntry,
    medicationId: Long? = null,
) {
    val viewModel: MedicationFormViewModel = viewModel(
        factory = AppViewModelFactory {
            MedicationFormViewModel(
                medicationRepository = container.medicationRepository,
                imageManager = container.imageManager,
                textRecognizer = container.textRecognizer,
                medicationId = medicationId,
            )
        },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var captureUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onImageSelected(it, ImageSource.GALLERY) }
    }
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            captureUri?.let { viewModel.onImageSelected(it, ImageSource.CAMERA) }
        }
    }

    val savedStateHandle = backStackEntry.savedStateHandle
    val ocrRaw by savedStateHandle.getStateFlow("ocr_raw", "").collectAsStateWithLifecycle()
    val ocrName by savedStateHandle.getStateFlow("ocr_name", "").collectAsStateWithLifecycle()
    val ocrDose by savedStateHandle.getStateFlow("ocr_dose", "").collectAsStateWithLifecycle()
    val ocrManufacturer by savedStateHandle.getStateFlow("ocr_manufacturer", "").collectAsStateWithLifecycle()
    val ocrInstructions by savedStateHandle.getStateFlow("ocr_instructions", "").collectAsStateWithLifecycle()

    LaunchedEffect(ocrRaw, ocrName, ocrDose, ocrManufacturer, ocrInstructions) {
        if (ocrRaw.isNotBlank()) {
            viewModel.applyOcrSuggestion(
                OcrSuggestion(
                    rawText = ocrRaw,
                    suggestedName = ocrName,
                    suggestedDosage = ocrDose,
                    suggestedManufacturer = ocrManufacturer,
                    suggestedInstructions = ocrInstructions,
                ),
            )
            savedStateHandle["ocr_raw"] = ""
            savedStateHandle["ocr_name"] = ""
            savedStateHandle["ocr_dose"] = ""
            savedStateHandle["ocr_manufacturer"] = ""
            savedStateHandle["ocr_instructions"] = ""
        }
    }

    LaunchedEffect(uiState.savedMedicationId) {
        val savedMedicationId = uiState.savedMedicationId
        if (savedMedicationId != null) {
            if (container.settingsRepository.settings.first().liveUpdatesEnabled) {
                container.reminderScheduler.nextLiveUpdatePayloadForMedication(savedMedicationId)?.let { payload ->
                    container.liveUpdateManager.startDoseLiveUpdate(payload)
                    container.reminderScheduler.scheduleLiveUpdateProgressTick(payload)
                }
            }
            viewModel.consumeSavedState()
            navController.popBackStack()
        }
    }

    AddMedicationScreen(
        uiState = uiState,
        isEditing = medicationId != null,
        onNameChange = viewModel::updateName,
        onDosageChange = viewModel::updateDosage,
        onFrequencyChange = viewModel::updateFrequency,
        onTimesChange = viewModel::updateTimesText,
        onStartDateChange = viewModel::updateStartDate,
        onEndDateChange = viewModel::updateEndDate,
        onInstructionsChange = viewModel::updateInstructions,
        onNotesChange = viewModel::updateNotes,
        onManufacturerChange = viewModel::updateManufacturer,
        onQuantityChange = viewModel::updateQuantity,
        onFormChange = viewModel::updateForm,
        onPickImage = {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onCaptureImage = {
            val uri = container.imageManager.createCaptureUri()
            captureUri = uri
            takePictureLauncher.launch(uri)
        },
        onOpenScan = { navController.navigate(Routes.ScanMedication) },
        onApplySuggestion = { uiState.draft.ocrSuggestion?.let(viewModel::applyOcrSuggestion) },
        onSave = viewModel::saveMedication,
        onBack = { navController.popBackStack() },
    )
}

@Composable
private fun PermissionsRoute(
    navController: androidx.navigation.NavHostController,
) {
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    PermissionsScreen(
        onRequestCamera = { cameraLauncher.launch(Manifest.permission.CAMERA) },
        onRequestNotifications = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onBack = { navController.popBackStack() },
    )
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.erivaldogelson.remedios.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erivaldogelson.remedios.domain.model.ActiveReminderSnapshot
import com.erivaldogelson.remedios.domain.model.AppThemeMode
import com.erivaldogelson.remedios.domain.model.DoseLogItemModel
import com.erivaldogelson.remedios.domain.model.HistoryFilter
import com.erivaldogelson.remedios.domain.model.SettingsSnapshot
import com.erivaldogelson.remedios.ui.components.AnimatedPrimaryActionButton
import com.erivaldogelson.remedios.ui.components.DoseLogItem
import com.erivaldogelson.remedios.ui.components.EmptyStateCard
import com.erivaldogelson.remedios.ui.components.PremiumScaffoldBackground
import com.erivaldogelson.remedios.ui.components.RoundedSettingsCard
import com.erivaldogelson.remedios.ui.components.SystemBackButton
import com.erivaldogelson.remedios.ui.i18n.LocalAppText
import com.erivaldogelson.remedios.ui.i18n.languageSubtitle
import com.erivaldogelson.remedios.ui.i18n.supportedLanguageOptions
import com.erivaldogelson.remedios.ui.theme.LocalRemediosHapticsEnabled
import com.erivaldogelson.remedios.ui.theme.Mist
import com.erivaldogelson.remedios.ui.theme.MistMuted
import com.erivaldogelson.remedios.ui.theme.RemediosTheme
import com.erivaldogelson.remedios.ui.theme.Warning
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

@Composable
fun HistoryScreen(
    items: List<DoseLogItemModel>,
    selectedFilter: HistoryFilter,
    onSelectFilter: (HistoryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = LocalAppText.current
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text.history.title, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onSelectFilter(filter) },
                        label = { Text(text.historyFilterLabel(filter)) },
                    )
                }
            }
            if (items.isEmpty()) {
                EmptyStateCard(
                    title = text.history.emptyTitle,
                    message = text.history.emptyMessage,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(items) { item ->
                        DoseLogItem(item = item)
                    }
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    settings: SettingsSnapshot,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onLiveUpdatesChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNowBarColorChange: (Long) -> Unit,
    onNowBarToneChange: (Int) -> Unit,
    onNavigationPillTransparencyChange: (Int) -> Unit,
    onOpenPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSection by remember { mutableStateOf<SettingsSection?>(null) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var backPreviewProgress by remember { mutableFloatStateOf(0f) }
    val previewOffset = with(LocalDensity.current) { 96.dp.toPx() }
    val text = LocalAppText.current.settings

    PredictiveBackHandler(enabled = selectedSection != null && !showLanguageSheet) { backEvents ->
        try {
            backEvents.collect { event ->
                backPreviewProgress = event.progress
            }
            selectedSection = null
        } finally {
            backPreviewProgress = 0f
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedSection != null && backPreviewProgress > 0f) {
            SettingsHome(
                onOpenAppearance = { selectedSection = SettingsSection.APPEARANCE },
                onOpenReminders = { selectedSection = SettingsSection.REMINDERS },
                onOpenAbout = { selectedSection = SettingsSection.ABOUT },
                onOpenLanguage = { showLanguageSheet = true },
                onOpenPermissions = onOpenPermissions,
                languageTag = settings.languageTag,
                modifier = Modifier.graphicsLayer {
                    alpha = backPreviewProgress
                    val previewScale = 0.96f + backPreviewProgress * 0.04f
                    scaleX = previewScale
                    scaleY = previewScale
                },
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = previewOffset * backPreviewProgress
                    alpha = 1f - backPreviewProgress * 0.18f
                },
        ) {
            when (selectedSection) {
                SettingsSection.APPEARANCE -> SettingsSubpage(
                    title = text.appearance,
                    subtitle = text.settings,
                    onBack = { selectedSection = null },
                ) {
                    AppearanceSettingsContent(
                        settings = settings,
                        onThemeModeChange = onThemeModeChange,
                        onDynamicColorChange = onDynamicColorChange,
                        onNavigationPillTransparencyChange = onNavigationPillTransparencyChange,
                        onOpenLanguage = { showLanguageSheet = true },
                    )
                }

                SettingsSection.REMINDERS -> SettingsSubpage(
                    title = text.reminders,
                    subtitle = text.remindersSubpageSubtitle,
                    onBack = { selectedSection = null },
                ) {
                    ReminderSettingsContent(
                        settings = settings,
                        onLiveUpdatesChange = onLiveUpdatesChange,
                        onHapticsChange = onHapticsChange,
                        onNowBarColorChange = onNowBarColorChange,
                        onNowBarToneChange = onNowBarToneChange,
                        onOpenPermissions = onOpenPermissions,
                    )
                }

                SettingsSection.ABOUT -> SettingsSubpage(
                    title = text.about,
                    subtitle = LocalAppText.current.common.appName,
                    onBack = { selectedSection = null },
                ) {
                    AboutSettingsContent()
                }

                null -> SettingsHome(
                    onOpenAppearance = { selectedSection = SettingsSection.APPEARANCE },
                    onOpenReminders = { selectedSection = SettingsSection.REMINDERS },
                    onOpenAbout = { selectedSection = SettingsSection.ABOUT },
                    onOpenLanguage = { showLanguageSheet = true },
                    onOpenPermissions = onOpenPermissions,
                    languageTag = settings.languageTag,
                )
            }
        }
    }

    if (showLanguageSheet) {
        LanguagePickerSheet(
            selectedLanguageTag = settings.languageTag,
            title = text.chooseLanguage,
            onSelectLanguage = { tag ->
                onLanguageChange(tag)
                showLanguageSheet = false
            },
            onDismiss = { showLanguageSheet = false },
        )
    }
}

private enum class SettingsSection {
    APPEARANCE,
    REMINDERS,
    ABOUT,
}

@Composable
private fun SettingsHome(
    onOpenAppearance: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenPermissions: () -> Unit,
    languageTag: String,
    modifier: Modifier = Modifier,
) {
    val appText = LocalAppText.current
    val text = appText.settings
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text.settings, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(
                text.settingsIntro,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SettingsMenuCard(
                icon = "💊",
                title = "${appText.common.appName}+",
                subtitle = text.remediesPlusSubtitle,
                highlighted = true,
                onClick = onOpenReminders,
            )
            SettingsMenuCard(
                icon = "⏱",
                title = text.reminders,
                subtitle = text.remindersSubtitle,
                onClick = onOpenReminders,
            )
            SettingsMenuCard(
                icon = "🎨",
                title = text.appearance,
                subtitle = text.appearanceSubtitle,
                onClick = onOpenAppearance,
            )
            SettingsMenuCard(
                icon = "ℹ️",
                title = text.about,
                subtitle = text.aboutSubtitle,
                onClick = onOpenAbout,
            )
            SettingsMenuCard(
                icon = "🌐",
                title = text.language,
                subtitle = languageSubtitle(languageTag, appText),
                onClick = onOpenLanguage,
            )
            AnimatedPrimaryActionButton(
                text = text.managePermissions,
                onClick = onOpenPermissions,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Rounded.Shield,
            )
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun SettingsSubpage(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SystemBackButton(onBack = onBack)
            Spacer(Modifier.height(32.dp))
            Text(title, style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            content()
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun SettingsMenuCard(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val hapticsEnabled = LocalRemediosHapticsEnabled.current
    val background = if (highlighted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.88f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            if (hapticsEnabled) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onClick?.invoke()
        },
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = background,
            disabledContainerColor = background,
        ),
        shape = RoundedCornerShape(34.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(icon, style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (onClick != null) {
                Text("›", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AppearanceSettingsContent(
    settings: SettingsSnapshot,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onNavigationPillTransparencyChange: (Int) -> Unit,
    onOpenLanguage: () -> Unit,
) {
    val appText = LocalAppText.current
    val text = appText.settings
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(34.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(text.theme, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.themeMode == AppThemeMode.SYSTEM,
                        onClick = { onThemeModeChange(AppThemeMode.SYSTEM) },
                        label = { Text(text.themeAuto) },
                    )
                    FilterChip(
                        selected = settings.themeMode == AppThemeMode.LIGHT,
                        onClick = { onThemeModeChange(AppThemeMode.LIGHT) },
                        label = { Text(text.themeLight) },
                    )
                    FilterChip(
                        selected = settings.themeMode == AppThemeMode.DARK,
                        onClick = { onThemeModeChange(AppThemeMode.DARK) },
                        label = { Text(text.themeDark) },
                    )
                }
            }
        }
        RoundedSettingsCard(
            title = text.dynamicColors,
            subtitle = text.dynamicColorsSubtitle,
            trailing = { Switch(checked = settings.dynamicColorEnabled, onCheckedChange = onDynamicColorChange) },
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(34.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text.pillTransparency,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "${settings.navigationPillTransparency}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(
                    value = settings.navigationPillTransparency.toFloat(),
                    onValueChange = { onNavigationPillTransparencyChange(it.toInt()) },
                    valueRange = 0f..90f,
                )
            }
        }
        RoundedSettingsCard(
            title = text.language,
            subtitle = languageSubtitle(settings.languageTag, appText),
            trailing = { Text("›", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            onClick = onOpenLanguage,
        )
    }
}

@Composable
private fun ReminderSettingsContent(
    settings: SettingsSnapshot,
    onLiveUpdatesChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onNowBarColorChange: (Long) -> Unit,
    onNowBarToneChange: (Int) -> Unit,
    onOpenPermissions: () -> Unit,
) {
    val text = LocalAppText.current.settings
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        RoundedSettingsCard(
            title = text.nowBarLiveUpdates,
            subtitle = text.nowBarLiveUpdatesSubtitle,
            trailing = { Switch(checked = settings.liveUpdatesEnabled, onCheckedChange = onLiveUpdatesChange) },
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(34.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(text.nowBarColor, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    nowBarColorOptions.forEach { option ->
                        Surface(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .clickable { onNowBarColorChange(option.argb) },
                            shape = CircleShape,
                            color = Color(option.argb),
                        ) {
                            if (settings.nowBarColor == option.argb) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Check, contentDescription = option.label, tint = Color.White)
                                }
                            }
                        }
                    }
                }
                Text("${text.nowBarTone}: ${settings.nowBarTone}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = settings.nowBarTone.toFloat(),
                    onValueChange = { onNowBarToneChange(it.toInt()) },
                    valueRange = 0f..100f,
                )
            }
        }
        RoundedSettingsCard(
            title = text.hapticFeedback,
            subtitle = text.hapticFeedbackSubtitle,
            trailing = { Switch(checked = settings.hapticsEnabled, onCheckedChange = onHapticsChange) },
        )
        RoundedSettingsCard(
            title = text.permissions,
            subtitle = text.permissionsSubtitle,
            trailing = { Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground) },
            onClick = onOpenPermissions,
        )
    }
}

private data class NowBarColorOption(val label: String, val argb: Long)

private val nowBarColorOptions = listOf(
    NowBarColorOption("Lavanda", 0xFFAA8CFF),
    NowBarColorOption("Azul", 0xFF5D7CFA),
    NowBarColorOption("Verde", 0xFF008577),
    NowBarColorOption("Menta", 0xFF4D8F7A),
    NowBarColorOption("Rosa", 0xFFD85D8F),
    NowBarColorOption("Âmbar", 0xFFC77822),
)

@Composable
private fun LanguagePickerSheet(
    selectedLanguageTag: String,
    title: String,
    onSelectLanguage: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val text = LocalAppText.current
    val languages = remember { supportedLanguageOptions }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(languages) { language ->
                    val selected = language.tag == selectedLanguageTag
                    val titleText = if (language.tag == "system") text.common.system else language.title
                    val subtitleText = if (language.tag == "system") text.common.useSystemLanguage else language.subtitle
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLanguage(language.tag) },
                        shape = RoundedCornerShape(24.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
                        },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(titleText, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                                if (subtitleText.isNotBlank()) {
                                    Text(subtitleText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (selected) {
                                Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AboutSettingsContent() {
    val context = LocalContext.current
    val text = LocalAppText.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(34.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("💊", style = MaterialTheme.typography.displaySmall)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text.common.appName, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                    Text("Now Bar • Live Updates", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(34.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "Erivaldo Gelson da Rocha João",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(text.settings.developer, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SocialLinkButton("IG") {
                        openUrl(context, "https://www.instagram.com/erivaldo_gelson?igsh=MWY5bWRqenNybnpzZg==")
                    }
                    SocialLinkButton("TH") {
                        openUrl(context, "https://www.threads.com/@erivaldo_gelson")
                    }
                    SocialLinkButton("GH") {
                        openUrl(context, "https://github.com/Erivaldogelson")
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialLinkButton(
    label: String,
    onClick: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val hapticsEnabled = LocalRemediosHapticsEnabled.current
    Surface(
        modifier = Modifier
            .height(52.dp)
            .width(76.dp)
            .clickable {
                if (hapticsEnabled) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onClick()
            },
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

@Composable
fun PermissionsScreen(
    onRequestCamera: () -> Unit,
    onRequestNotifications: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = LocalAppText.current.permissions
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text.title) },
                    navigationIcon = {
                        SystemBackButton(onBack = onBack, modifier = Modifier.padding(start = 12.dp))
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
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                EmptyStateCard(
                    title = text.cameraTitle,
                    message = text.cameraMessage,
                )
                AnimatedPrimaryActionButton(
                    text = text.allowCamera,
                    onClick = onRequestCamera,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Notifications,
                )
                EmptyStateCard(
                    title = text.notificationsTitle,
                    message = text.notificationsMessage,
                )
                AnimatedPrimaryActionButton(
                    text = text.allowNotifications,
                    onClick = onRequestNotifications,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Notifications,
                    containerColor = Warning,
                )
            }
        }
    }
}

@Composable
fun ActiveReminderScreen(
    activeReminder: ActiveReminderSnapshot?,
    onTakeNow: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = LocalAppText.current
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text.activeReminder.title) },
                    navigationIcon = {
                        SystemBackButton(onBack = onBack, modifier = Modifier.padding(start = 12.dp))
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
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                if (activeReminder == null) {
                    EmptyStateCard(
                        title = text.activeReminder.emptyTitle,
                        message = text.activeReminder.emptyMessage,
                    )
                } else {
                    var now by remember(activeReminder.reminderId) { mutableStateOf(LocalDateTime.now()) }
                    LaunchedEffect(activeReminder.reminderId) {
                        while (true) {
                            now = LocalDateTime.now()
                            delay(1_000)
                        }
                    }
                    val totalMillis = Duration.between(activeReminder.triggerAt, activeReminder.expiresAt)
                        .toMillis()
                        .coerceAtLeast(1L)
                    val elapsedMillis = Duration.between(activeReminder.triggerAt, now)
                        .toMillis()
                        .coerceIn(0L, totalMillis)
                    val progress = elapsedMillis / totalMillis.toFloat()
                    val remainingMinutes = Duration.between(now, activeReminder.expiresAt)
                        .toMinutes()
                        .coerceAtLeast(0L)
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

                    Text(text.activeReminder.activeDose, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        activeReminder.medicationName,
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(activeReminder.dosage, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    EmptyStateCard(
                        title = text.activeReminder.registrationWindow,
                        message = text.activeReminder.registrationWindowMessage.format(
                            remainingMinutes,
                            activeReminder.expiresAt.format(timeFormatter),
                        ),
                    )
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = Warning,
                        trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f),
                    )
                    AnimatedPrimaryActionButton(
                        text = text.common.takeDose,
                        onClick = onTakeNow,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AnimatedPrimaryActionButton(
                            text = text.common.snooze,
                            onClick = onSnooze,
                            modifier = Modifier.weight(1f),
                            containerColor = Warning,
                        )
                        AnimatedPrimaryActionButton(
                            text = text.common.skip,
                            onClick = onSkip,
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.16f),
                            contentColor = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun HistoryPreview() {
    RemediosTheme {
        HistoryScreen(
            items = PreviewData.historyItems,
            selectedFilter = HistoryFilter.DAY,
            onSelectFilter = { _ -> },
        )
    }
}

@Preview
@Composable
private fun SettingsPreview() {
    RemediosTheme {
        SettingsScreen(
            settings = PreviewData.settings,
            onThemeModeChange = { _ -> },
            onDynamicColorChange = { _ -> },
            onLiveUpdatesChange = { _ -> },
            onHapticsChange = { _ -> },
            onLanguageChange = { _ -> },
            onNowBarColorChange = { _ -> },
            onNowBarToneChange = { _ -> },
            onNavigationPillTransparencyChange = { _ -> },
            onOpenPermissions = {},
        )
    }
}

@Preview
@Composable
private fun ActiveReminderPreview() {
    RemediosTheme {
        ActiveReminderScreen(
            activeReminder = PreviewData.dashboard.activeReminder,
            onTakeNow = {},
            onSnooze = {},
            onSkip = {},
            onBack = {},
        )
    }
}

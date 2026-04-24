@file:OptIn(ExperimentalMaterial3Api::class)

package com.erivaldogelson.remedios.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
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
import com.erivaldogelson.remedios.ui.theme.LocalRemediosHapticsEnabled
import com.erivaldogelson.remedios.ui.theme.Mist
import com.erivaldogelson.remedios.ui.theme.MistMuted
import com.erivaldogelson.remedios.ui.theme.RemediosTheme
import com.erivaldogelson.remedios.ui.theme.Warning
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@Composable
fun HistoryScreen(
    items: List<DoseLogItemModel>,
    selectedFilter: HistoryFilter,
    onSelectFilter: (HistoryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Histórico", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onSelectFilter(filter) },
                        label = { Text(filter.name.lowercase().replaceFirstChar(Char::titlecase)) },
                    )
                }
            }
            if (items.isEmpty()) {
                EmptyStateCard(
                    title = "Ainda sem registros",
                    message = "Quando você marcar doses como tomadas, adiadas ou perdidas, tudo aparece aqui.",
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
    onOpenPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSection by remember { mutableStateOf<SettingsSection?>(null) }

    when (selectedSection) {
        SettingsSection.APPEARANCE -> SettingsSubpage(
            title = "Aparência",
            subtitle = "Configurações",
            onBack = { selectedSection = null },
            modifier = modifier,
        ) {
            AppearanceSettingsContent(
                settings = settings,
                onThemeModeChange = onThemeModeChange,
                onDynamicColorChange = onDynamicColorChange,
            )
        }

        SettingsSection.REMINDERS -> SettingsSubpage(
            title = "Lembretes",
            subtitle = "Now Bar, feedback e permissões",
            onBack = { selectedSection = null },
            modifier = modifier,
        ) {
            ReminderSettingsContent(
                settings = settings,
                onLiveUpdatesChange = onLiveUpdatesChange,
                onHapticsChange = onHapticsChange,
                onOpenPermissions = onOpenPermissions,
            )
        }

        SettingsSection.ABOUT -> SettingsSubpage(
            title = "Sobre",
            subtitle = "Remédios",
            onBack = { selectedSection = null },
            modifier = modifier,
        ) {
            AboutSettingsContent()
        }

        null -> SettingsHome(
            onOpenAppearance = { selectedSection = SettingsSection.APPEARANCE },
            onOpenReminders = { selectedSection = SettingsSection.REMINDERS },
            onOpenAbout = { selectedSection = SettingsSection.ABOUT },
            onOpenPermissions = onOpenPermissions,
            modifier = modifier,
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
    onOpenPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Configurações", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(
                "Menu organizado para aparência, lembretes, Now Bar e informações do app.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SettingsMenuCard(
                icon = "💊",
                title = "Remédios+",
                subtitle = "Ativação por remédio e Now Bar",
                highlighted = true,
                onClick = onOpenReminders,
            )
            SettingsMenuCard(
                icon = "⏱",
                title = "Lembretes",
                subtitle = "Live Updates, feedback tátil e permissões",
                onClick = onOpenReminders,
            )
            SettingsMenuCard(
                icon = "🎨",
                title = "Aparência",
                subtitle = "Tema claro, escuro e cores dinâmicas",
                onClick = onOpenAppearance,
            )
            SettingsMenuCard(
                icon = "ℹ️",
                title = "Sobre",
                subtitle = "Desenvolvedor e redes sociais",
                onClick = onOpenAbout,
            )
            SettingsMenuCard(
                icon = "🌐",
                title = "Idioma",
                subtitle = "Português do Brasil",
            )
            AnimatedPrimaryActionButton(
                text = "Gerenciar permissões",
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
            CircularBackButton(onBack = onBack)
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
private fun CircularBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val hapticsEnabled = LocalRemediosHapticsEnabled.current
    Surface(
        modifier = modifier
            .size(64.dp)
            .clickable {
                if (hapticsEnabled) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onBack()
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Voltar",
                tint = MaterialTheme.colorScheme.onBackground,
            )
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
) {
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
                Text("Tema", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.themeMode == AppThemeMode.SYSTEM,
                        onClick = { onThemeModeChange(AppThemeMode.SYSTEM) },
                        label = { Text("Auto") },
                    )
                    FilterChip(
                        selected = settings.themeMode == AppThemeMode.LIGHT,
                        onClick = { onThemeModeChange(AppThemeMode.LIGHT) },
                        label = { Text("Claro") },
                    )
                    FilterChip(
                        selected = settings.themeMode == AppThemeMode.DARK,
                        onClick = { onThemeModeChange(AppThemeMode.DARK) },
                        label = { Text("Escuro") },
                    )
                }
            }
        }
        RoundedSettingsCard(
            title = "Cores dinâmicas",
            subtitle = "Adapta detalhes do app às cores do dispositivo quando disponível.",
            trailing = { Switch(checked = settings.dynamicColorEnabled, onCheckedChange = onDynamicColorChange) },
        )
    }
}

@Composable
private fun ReminderSettingsContent(
    settings: SettingsSnapshot,
    onLiveUpdatesChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onOpenPermissions: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        RoundedSettingsCard(
            title = "Now Bar / Live Updates",
            subtitle = "Só ativa quando existir remédio salvo com próxima dose.",
            trailing = { Switch(checked = settings.liveUpdatesEnabled, onCheckedChange = onLiveUpdatesChange) },
        )
        RoundedSettingsCard(
            title = "Feedback tátil",
            subtitle = "Aplica vibração suave em botões, navegação e cartões.",
            trailing = { Switch(checked = settings.hapticsEnabled, onCheckedChange = onHapticsChange) },
        )
        RoundedSettingsCard(
            title = "Permissões",
            subtitle = "Câmera, notificações e alarmes exatos.",
            trailing = { Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground) },
            onClick = onOpenPermissions,
        )
    }
}

@Composable
private fun AboutSettingsContent() {
    val context = LocalContext.current
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
                    Text("Remédios", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
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
                Text("Desenvolvedor", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Permissões") },
                    navigationIcon = {
                        CircularBackButton(onBack = onBack, modifier = Modifier.padding(start = 12.dp))
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
                    title = "Câmera",
                    message = "Necessária para tirar foto do remédio e escanear a embalagem com OCR.",
                )
                AnimatedPrimaryActionButton(
                    text = "Permitir câmera",
                    onClick = onRequestCamera,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Notifications,
                )
                EmptyStateCard(
                    title = "Notificações",
                    message = "Essenciais para lembretes locais, ações rápidas e Live Updates compatíveis.",
                )
                AnimatedPrimaryActionButton(
                    text = "Permitir notificações",
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
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Lembrete ativo") },
                    navigationIcon = {
                        CircularBackButton(onBack = onBack, modifier = Modifier.padding(start = 12.dp))
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
                        title = "Nenhum lembrete ativo",
                        message = "Quando um horário entrar em andamento, ele aparece aqui com ações rápidas.",
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

                    Text("Dose em andamento", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        activeReminder.medicationName,
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(activeReminder.dosage, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    EmptyStateCard(
                        title = "Janela de registro",
                        message = "Restam ${remainingMinutes} min para registrar. Ativa at? ${activeReminder.expiresAt.format(timeFormatter)} com a??es r?pidas e Now Bar quando compat?vel.",
                    )
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = Warning,
                        trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f),
                    )
                    AnimatedPrimaryActionButton(
                        text = "Registrar tomada",
                        onClick = onTakeNow,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AnimatedPrimaryActionButton(
                            text = "Adiar",
                            onClick = onSnooze,
                            modifier = Modifier.weight(1f),
                            containerColor = Warning,
                        )
                        AnimatedPrimaryActionButton(
                            text = "Ignorar",
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

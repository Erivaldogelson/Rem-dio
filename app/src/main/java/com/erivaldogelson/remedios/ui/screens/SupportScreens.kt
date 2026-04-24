@file:OptIn(ExperimentalMaterial3Api::class)

package com.erivaldogelson.remedios.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
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
            Text("Histórico", style = MaterialTheme.typography.displayMedium, color = Mist)
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
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Configurações", style = MaterialTheme.typography.displayMedium, color = Mist)
            Text(
                "Tema, idioma, permissões, backup e a base para Live Updates.",
                style = MaterialTheme.typography.bodyLarge,
                color = MistMuted,
            )
            RoundedSettingsCard(
                title = "Tema",
                subtitle = "Escuro premium por padrão, com opção de seguir o sistema.",
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = settings.themeMode == AppThemeMode.DARK,
                            onClick = { onThemeModeChange(AppThemeMode.DARK) },
                            label = { Text("Escuro") },
                        )
                        FilterChip(
                            selected = settings.themeMode == AppThemeMode.SYSTEM,
                            onClick = { onThemeModeChange(AppThemeMode.SYSTEM) },
                            label = { Text("Sistema") },
                        )
                    }
                },
            )
            RoundedSettingsCard(
                title = "Material You / cores dinâmicas",
                subtitle = "Ajusta detalhes do app às cores do dispositivo quando disponível.",
                trailing = { Switch(checked = settings.dynamicColorEnabled, onCheckedChange = onDynamicColorChange) },
            )
            RoundedSettingsCard(
                title = "Now Bar / Live Updates",
                subtitle = "Promove lembretes ativos para superfícies compatíveis do Android 16.",
                trailing = { Switch(checked = settings.liveUpdatesEnabled, onCheckedChange = onLiveUpdatesChange) },
            )
            RoundedSettingsCard(
                title = "Feedback tátil",
                subtitle = "Microinterações suaves ao marcar doses e navegar.",
                trailing = { Switch(checked = settings.hapticsEnabled, onCheckedChange = onHapticsChange) },
            )
            RoundedSettingsCard(
                title = "Permissões",
                subtitle = "Câmera, notificações e alarmes exatos.",
                trailing = { Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Mist) },
                onClick = onOpenPermissions,
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
                        IconButton(onClick = onBack) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = Mist,
                        navigationIconContentColor = Mist,
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
                        IconButton(onClick = onBack) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = Mist,
                        navigationIconContentColor = Mist,
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

                    Text("Dose em andamento", style = MaterialTheme.typography.displayMedium, color = Mist)
                    Text(
                        activeReminder.medicationName,
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                        color = Mist,
                    )
                    Text(activeReminder.dosage, style = MaterialTheme.typography.titleLarge, color = MistMuted)
                    EmptyStateCard(
                        title = "Janela de registro",
                        message = "Restam ${remainingMinutes} min para registrar. Ativa at? ${activeReminder.expiresAt.format(timeFormatter)} com a??es r?pidas e Now Bar quando compat?vel.",
                    )
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = Warning,
                        trackColor = Mist.copy(alpha = 0.14f),
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
                            containerColor = Mist.copy(alpha = 0.16f),
                            contentColor = Mist,
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

package com.erivaldogelson.remedios.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erivaldogelson.remedios.domain.model.DashboardSnapshot
import com.erivaldogelson.remedios.ui.components.AnimatedPrimaryActionButton
import com.erivaldogelson.remedios.ui.components.EmptyStateCard
import com.erivaldogelson.remedios.ui.components.NextDoseCircle
import com.erivaldogelson.remedios.ui.components.PremiumScaffoldBackground
import com.erivaldogelson.remedios.ui.i18n.LocalAppText
import com.erivaldogelson.remedios.ui.theme.InkCard
import com.erivaldogelson.remedios.ui.theme.Mist
import com.erivaldogelson.remedios.ui.theme.MistMuted
import com.erivaldogelson.remedios.ui.theme.RemediosTheme
import com.erivaldogelson.remedios.ui.theme.Warning

@Composable
fun DashboardScreen(
    state: DashboardSnapshot,
    onTakeNow: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit,
    onOpenMedications: () -> Unit,
    onOpenActiveReminder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = LocalAppText.current
    val title = if (state.activeReminder != null || (state.nextDose?.remainingMinutes ?: 1L) <= 0) {
        text.dashboard.activeDoseTitle
    } else {
        text.dashboard.nextDoseTitle
    }
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val nextDoseSize = when {
                maxWidth >= 900.dp -> 320.dp
                maxWidth >= 720.dp -> 300.dp
                else -> 290.dp
            }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .widthIn(max = 920.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = text.dashboard.intro,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            NextDoseCircle(
                nextDose = state.nextDose,
                modifier = Modifier
                    .size(nextDoseSize)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 10.dp),
            )

            if (state.nextDose != null || state.activeReminder != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedPrimaryActionButton(
                        text = text.common.takeDose,
                        onClick = onTakeNow,
                        icon = Icons.Rounded.LocalHospital,
                        modifier = Modifier.weight(1f),
                    )
                    AnimatedPrimaryActionButton(
                        text = text.common.snooze,
                        onClick = onSnooze,
                        icon = Icons.Rounded.HourglassTop,
                        modifier = Modifier.weight(1f),
                        containerColor = Warning,
                    )
                }
                AnimatedPrimaryActionButton(
                    text = text.common.skip,
                    onClick = onSkip,
                    icon = Icons.Rounded.AlarmOn,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
                    contentColor = MaterialTheme.colorScheme.background,
                )
            }

            state.nextDose?.let { nextDose ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text.dashboard.next, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(nextDose.medicationName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                        Text(
                            "${nextDose.dosage} • ${nextDose.scheduledAt.toLocalTime()}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } ?: EmptyStateCard(
                title = text.dashboard.emptyTitle,
                message = text.dashboard.emptyMessage,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(
                    title = text.dashboard.dosesToday,
                    value = state.dueTodayCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    title = text.dashboard.pending,
                    value = state.pendingTodayCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }

            if (state.activeReminder != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large,
                    onClick = onOpenActiveReminder,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text.dashboard.activeReminder, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text(
                                state.activeReminder.medicationName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(Icons.Rounded.AlarmOn, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(22.dp))
                    }
                }
            }

            AnimatedPrimaryActionButton(
                text = text.dashboard.viewMedications,
                onClick = onOpenMedications,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(90.dp))
        }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Preview
@Composable
private fun DashboardPreview() {
    RemediosTheme {
        DashboardScreen(
            state = PreviewData.dashboard,
            onTakeNow = {},
            onSnooze = {},
            onSkip = {},
            onOpenMedications = {},
            onOpenActiveReminder = {},
        )
    }
}

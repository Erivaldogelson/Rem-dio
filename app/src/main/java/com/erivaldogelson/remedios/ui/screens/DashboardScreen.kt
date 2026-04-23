package com.erivaldogelson.remedios.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = state.greetingTitle,
                style = MaterialTheme.typography.displayLarge,
                color = Mist,
            )
            Text(
                text = "Tudo o que importa para a próxima medicação, num lugar só.",
                style = MaterialTheme.typography.bodyLarge,
                color = MistMuted,
            )

            NextDoseCircle(
                nextDose = state.nextDose,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnimatedPrimaryActionButton(
                    text = "Tomar agora",
                    onClick = onTakeNow,
                    icon = Icons.Rounded.LocalHospital,
                    modifier = Modifier.weight(1f),
                )
                AnimatedPrimaryActionButton(
                    text = "Adiar",
                    onClick = onSnooze,
                    icon = Icons.Rounded.HourglassTop,
                    modifier = Modifier.weight(1f),
                    containerColor = Warning,
                )
            }
            AnimatedPrimaryActionButton(
                text = "Pular dose",
                onClick = onSkip,
                icon = Icons.Rounded.AlarmOn,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Mist.copy(alpha = 0.88f),
            )

            state.nextDose?.let { nextDose ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = InkCard),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Próximo", style = MaterialTheme.typography.titleMedium, color = MistMuted)
                        Text(nextDose.medicationName, style = MaterialTheme.typography.titleLarge, color = Mist)
                        Text(
                            "${nextDose.dosage} • ${nextDose.scheduledAt.toLocalTime()}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MistMuted,
                        )
                    }
                }
            } ?: EmptyStateCard(
                title = "Nenhuma dose próxima",
                message = "Adicione seu primeiro medicamento para começar a receber lembretes, histórico e atualizações ao vivo.",
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(
                    title = "Doses hoje",
                    value = state.dueTodayCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    title = "Pendentes",
                    value = state.pendingTodayCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }

            if (state.activeReminder != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = InkCard),
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
                            Text("Lembrete ativo", style = MaterialTheme.typography.titleMedium, color = Mist)
                            Text(
                                state.activeReminder.medicationName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MistMuted,
                            )
                        }
                        Icon(Icons.Rounded.AlarmOn, contentDescription = null, tint = Mist, modifier = Modifier.size(22.dp))
                    }
                }
            }

            AnimatedPrimaryActionButton(
                text = "Ver medicamentos",
                onClick = onOpenMedications,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Mist.copy(alpha = 0.12f),
                contentColor = Mist,
            )
            Spacer(Modifier.height(90.dp))
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
        colors = CardDefaults.cardColors(containerColor = InkCard),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MistMuted)
            Text(value, style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold), color = Mist)
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

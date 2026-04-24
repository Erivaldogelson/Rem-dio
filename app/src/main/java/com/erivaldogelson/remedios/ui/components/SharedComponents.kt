package com.erivaldogelson.remedios.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.erivaldogelson.remedios.domain.model.DoseLogItemModel
import com.erivaldogelson.remedios.domain.model.DoseStatus
import com.erivaldogelson.remedios.domain.model.MedicationSummary
import com.erivaldogelson.remedios.domain.model.NextDoseSnapshot
import com.erivaldogelson.remedios.domain.model.OcrSuggestion
import com.erivaldogelson.remedios.ui.theme.Danger
import com.erivaldogelson.remedios.ui.theme.InkCard
import com.erivaldogelson.remedios.ui.theme.InkCardSoft
import com.erivaldogelson.remedios.ui.theme.Lavender
import com.erivaldogelson.remedios.ui.theme.Mint
import com.erivaldogelson.remedios.ui.theme.Mist
import com.erivaldogelson.remedios.ui.theme.MistMuted
import com.erivaldogelson.remedios.ui.theme.OutlineSoft
import com.erivaldogelson.remedios.ui.theme.SoftLilac
import com.erivaldogelson.remedios.ui.theme.Warning
import java.time.format.DateTimeFormatter

data class BottomBarItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun PremiumScaffoldBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    colorScheme.background,
                    colorScheme.surfaceVariant.copy(alpha = 0.88f),
                    colorScheme.background,
                ),
            ),
        ),
    ) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(colorScheme.primary.copy(alpha = 0.22f), Color.Transparent),
                    ),
                    shape = CircleShape,
                )
                .align(Alignment.TopCenter),
        )
        content()
    }
}

@Composable
fun PillBottomNavigation(
    items: List<BottomBarItem>,
    selectedRoute: String,
    onSelect: (BottomBarItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
        shape = RoundedCornerShape(36.dp),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items.forEach { item ->
                val selected = item.route == selectedRoute
                val itemWeight by animateFloatAsState(
                    targetValue = if (selected) 2.2f else 1f,
                    label = "pill_item_weight",
                )
                val weightModifier = Modifier.weight(itemWeight)
                NavigationItem(weightModifier, item, selected) { onSelect(item) }
            }
        }
    }
}

@Composable
private fun RowScope.NavigationItem(
    modifier: Modifier,
    item: BottomBarItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (selected) 0.22f else 0f,
        label = "pill_item_background",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        label = "pill_icon_scale",
    )
    val background = SoftLilac.copy(alpha = backgroundAlpha)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = if (selected) 10.dp else 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.scale(iconScale),
        )
        if (selected) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun MedicationCard(
    medication: MedicationSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(30.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(medication.accentColor).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                if (medication.imageUri != null) {
                    AsyncImage(
                        model = medication.imageUri,
                        contentDescription = medication.name,
                        modifier = Modifier.matchParentSize(),
                    )
                } else {
                    Text(
                        text = when (medication.form.name) {
                            "SYRUP" -> "🧴"
                            "INJECTION" -> "💉"
                            else -> "💊"
                        },
                        style = MaterialTheme.typography.headlineLarge,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${medication.dosage}  •  Próximo ${medication.nextTimeLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusPill(
                text = "${medication.quantityRemaining}",
                icon = Icons.Rounded.CheckCircle,
                background = Color(medication.accentColor).copy(alpha = 0.14f),
                tint = Color(medication.accentColor),
            )
            onEdit?.let { edit ->
                IconButton(onClick = edit) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = "Editar remédio salvo",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            onDelete?.let { delete ->
                IconButton(onClick = delete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Apagar remédio salvo",
                        tint = Danger,
                    )
                }
            }
        }
    }
}

@Composable
fun NextDoseCircle(
    nextDose: NextDoseSnapshot?,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = nextDose?.progress ?: 0f,
        label = "dose_progress",
    )
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawArc(
                color = outlineColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 20f, cap = StrokeCap.Round),
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Color(0xFF8B71F6), Color(0xFFB999FF), Color(0xFFF9A8D4), Color(0xFF8B71F6)),
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = 20f, cap = StrokeCap.Round),
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = nextDose?.remainingMinutes?.let { "${it.coerceAtLeast(0)} min" } ?: "--",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = nextDose?.medicationName ?: "Sem doses próximas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            nextDose?.let {
                Text(
                    text = it.scheduledAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Lavender,
                )
            }
        }
    }
}

@Composable
fun AnimatedPrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = SoftLilac,
    contentColor: Color = Color(0xFF0D0B15),
    icon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (interactionSource.collectIsPressedAsState().value) 0.97f else 1f,
        label = "press_scale",
    )
    Row(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(28.dp))
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        icon?.let {
            Icon(it, contentDescription = null, tint = contentColor)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
        )
    }
}

@Composable
fun RoundedSettingsCard(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val cardContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(4.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
    if (onClick == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(28.dp),
        ) {
            cardContent()
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            onClick = onClick,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(28.dp),
        ) {
            cardContent()
        }
    }
}

@Composable
fun OcrResultConfirmationCard(
    suggestion: OcrSuggestion,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Sugestões do OCR", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    listOfNotNull(
                        suggestion.suggestedName.takeIf { it.isNotBlank() }?.let { "Nome: $it" },
                        suggestion.suggestedDosage.takeIf { it.isNotBlank() }?.let { "Dose: $it" },
                        suggestion.suggestedManufacturer.takeIf { it.isNotBlank() }?.let { "Lab: $it" },
                    ),
                ) { item ->
                    StatusPill(
                        text = item,
                        icon = Icons.Rounded.Schedule,
                        background = SoftLilac.copy(alpha = 0.14f),
                        tint = SoftLilac,
                    )
                }
            }
            if (suggestion.suggestedInstructions.isNotBlank()) {
                Text(
                    text = suggestion.suggestedInstructions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedPrimaryActionButton(
                text = "Aplicar sugestões",
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun DoseLogItem(
    item: DoseLogItemModel,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(26.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StatusPill(
                text = when (item.status) {
                    DoseStatus.TAKEN -> "Tomado"
                    DoseStatus.SNOOZED -> "Adiado"
                    DoseStatus.SKIPPED -> "Ignorado"
                    DoseStatus.MISSED -> "Perdido"
                    DoseStatus.UPCOMING -> "Próximo"
                },
                icon = when (item.status) {
                    DoseStatus.TAKEN -> Icons.Rounded.CheckCircle
                    DoseStatus.MISSED -> Icons.Rounded.History
                    else -> Icons.Rounded.Schedule
                },
                background = item.status.backgroundColor(),
                tint = item.status.tintColor(),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.medicationName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    "${item.dosage} • ${item.scheduledAt.format(DateTimeFormatter.ofPattern("dd/MM • HH:mm"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.note.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(item.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(30.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    icon: ImageVector,
    background: Color,
    tint: Color,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = tint)
    }
}

private fun DoseStatus.backgroundColor(): Color = when (this) {
    DoseStatus.TAKEN -> Mint.copy(alpha = 0.18f)
    DoseStatus.SNOOZED -> SoftLilac.copy(alpha = 0.16f)
    DoseStatus.SKIPPED -> Warning.copy(alpha = 0.18f)
    DoseStatus.MISSED -> Danger.copy(alpha = 0.18f)
    DoseStatus.UPCOMING -> Lavender.copy(alpha = 0.18f)
}

private fun DoseStatus.tintColor(): Color = when (this) {
    DoseStatus.TAKEN -> Mint
    DoseStatus.SNOOZED -> SoftLilac
    DoseStatus.SKIPPED -> Warning
    DoseStatus.MISSED -> Danger
    DoseStatus.UPCOMING -> Lavender
}

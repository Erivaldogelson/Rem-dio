package com.erivaldogelson.remedios.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.erivaldogelson.remedios.domain.model.AppThemeMode
import com.erivaldogelson.remedios.domain.model.SettingsSnapshot

private val RemediosShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(30.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(38.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(48.dp),
)

private val DarkPalette = darkColorScheme(
    primary = SoftLilac,
    onPrimary = Ink,
    primaryContainer = VioletAccent,
    onPrimaryContainer = Mist,
    secondary = Lavender,
    onSecondary = Ink,
    tertiary = RoseGlow,
    background = Ink,
    onBackground = Mist,
    surface = InkCard,
    onSurface = Mist,
    surfaceVariant = InkCardSoft,
    onSurfaceVariant = MistMuted,
    outline = OutlineSoft,
    error = Danger,
)

@Composable
fun RemediosTheme(
    settings: SettingsSnapshot = SettingsSnapshot(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val shouldUseDark = when (settings.themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val dynamicColors = settings.dynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColors && shouldUseDark -> dynamicDarkColorScheme(context)
        else -> DarkPalette
    }.copy(
        background = Ink,
        surface = InkCard,
        surfaceVariant = InkCardSoft,
        primary = if (dynamicColors && shouldUseDark) Color(context.getColor(android.R.color.system_accent1_200)) else SoftLilac,
        onPrimary = Ink,
    )

    @Suppress("UNUSED_EXPRESSION")
    (context as? Activity)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RemediosTypography,
        shapes = RemediosShapes,
        content = content,
    )
}

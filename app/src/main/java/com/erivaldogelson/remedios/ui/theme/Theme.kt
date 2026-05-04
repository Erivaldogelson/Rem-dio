package com.erivaldogelson.remedios.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
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

private val LightPalette = lightColorScheme(
    primary = VioletAccent,
    onPrimary = Color.White,
    primaryContainer = PorcelainCardSoft,
    onPrimaryContainer = InkText,
    secondary = ElectricLilac,
    onSecondary = Color.White,
    tertiary = RoseGlow,
    background = Porcelain,
    onBackground = InkText,
    surface = PorcelainCard,
    onSurface = InkText,
    surfaceVariant = PorcelainCardSoft,
    onSurfaceVariant = InkTextMuted,
    outline = OutlineLight,
    error = Color(0xFFB3261E),
)

val LocalRemediosHapticsEnabled = staticCompositionLocalOf { true }

@Composable
fun RemediosTheme(
    settings: SettingsSnapshot = SettingsSnapshot(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val shouldUseDark = when (settings.themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val dynamicColors = settings.dynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColors && shouldUseDark -> dynamicDarkColorScheme(context)
        dynamicColors && !shouldUseDark -> dynamicLightColorScheme(context)
        shouldUseDark -> DarkPalette
        else -> LightPalette
    }.copy(
        background = if (shouldUseDark) Ink else Porcelain,
        surface = if (shouldUseDark) InkCard else PorcelainCard,
        surfaceVariant = if (shouldUseDark) InkCardSoft else PorcelainCardSoft,
        outline = if (shouldUseDark) OutlineSoft else OutlineLight,
        primary = if (dynamicColors) {
            Color(context.getColor(android.R.color.system_accent1_200))
        } else if (shouldUseDark) {
            SoftLilac
        } else {
            VioletAccent
        },
        onPrimary = if (shouldUseDark) Ink else Color.White,
    )

    if (!view.isInEditMode) {
        SideEffect {
            (context as? Activity)?.window?.let { window ->
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !shouldUseDark
                    isAppearanceLightNavigationBars = !shouldUseDark
                }
            }
        }
    }

    CompositionLocalProvider(LocalRemediosHapticsEnabled provides settings.hapticsEnabled) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RemediosTypography,
            shapes = RemediosShapes,
            content = content,
        )
    }
}

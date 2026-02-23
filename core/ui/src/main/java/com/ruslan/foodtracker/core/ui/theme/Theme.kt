package com.ruslan.foodtracker.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Светлая цветовая схема Food Tracker
 * Согласно дизайн-спецификации
 */
private val LightColorScheme = lightColorScheme(
    // Primary colors
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = TextPrimaryLight,
    // Secondary colors
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = TextPrimaryLight,
    // Tertiary colors (используем Accent)
    tertiary = Accent,
    onTertiary = TextPrimaryLight,
    tertiaryContainer = Accent,
    onTertiaryContainer = TextPrimaryLight,
    // Error colors (используем Danger)
    error = Danger,
    onError = Color.White,
    errorContainer = Danger,
    onErrorContainer = Color.White,
    // Background colors
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    // Surface colors (карточки)
    surface = CardBackgroundLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = CardBackgroundLight,
    onSurfaceVariant = TextSecondaryLight,
    // Outline (границы)
    outline = BorderLight,
    outlineVariant = BorderLight
)

/**
 * Тёмная цветовая схема Food Tracker
 * Согласно дизайн-спецификации
 */
private val DarkColorScheme = darkColorScheme(
    // Primary colors (остаются яркими в тёмной теме)
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = Color.White,
    // Secondary colors (остаются яркими в тёмной теме)
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Secondary,
    onSecondaryContainer = Color.White,
    // Tertiary colors (используем Accent)
    tertiary = Accent,
    onTertiary = TextPrimaryDark,
    tertiaryContainer = Accent,
    onTertiaryContainer = TextPrimaryDark,
    // Error colors (используем Danger)
    error = Danger,
    onError = Color.White,
    errorContainer = Danger,
    onErrorContainer = Color.White,
    // Background colors
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    // Surface colors (карточки)
    surface = CardBackgroundDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardBackgroundDark,
    onSurfaceVariant = TextSecondaryDark,
    // Outline (границы)
    outline = BorderDark,
    outlineVariant = BorderDark
)

/**
 * FoodTrackerTheme - основная тема приложения
 *
 * @param darkTheme использовать тёмную тему (по умолчанию - системная)
 * @param content контент приложения
 */
@Composable
fun FoodTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

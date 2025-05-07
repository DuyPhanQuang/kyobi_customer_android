package com.kyobi.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.Typography

data class AppThemeConfig(
    val colors: AppColors,
    val typography: Typography,
    val shapes: Shapes,
    val spacing: Dimension,
    val icon: Icon,
    val appBar: AppBar,
    val width: Dimension,
    val height: Dimension,
)

val LocalTheme = staticCompositionLocalOf<AppThemeConfig> {
    error("Missing AppThemeConfig provided")
}

// Light Theme Config
val LightThemeConfig = AppThemeConfig(
    colors = LightAppColors,
    typography = AppTypography,
    shapes = AppShapes,
    spacing = Dimension,
    icon = Icon,
    appBar = AppBar,
    width = Dimension,
    height = Dimension
)

// Dark Theme Config
val DarkThemeConfig = AppThemeConfig(
    colors = DarkAppColors,
    typography = AppTypography,
    shapes = AppShapes,
    spacing = Dimension,
    icon = Icon,
    appBar = AppBar,
    width = Dimension,
    height = Dimension
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val themeConfig = if (darkTheme) DarkThemeConfig else LightThemeConfig
    val colorScheme = if (darkTheme) themeConfig.colors.toDarkColorScheme() else themeConfig.colors.toColorScheme()

    CompositionLocalProvider(LocalTheme provides themeConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = themeConfig.typography,
            shapes = themeConfig.shapes,
            content = content,
        )
    }
}

val MaterialTheme.kyobiTheme: AppThemeConfig
    @Composable
    get() = LocalTheme.current

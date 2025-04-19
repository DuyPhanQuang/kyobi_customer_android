package com.kyobi.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class Colors(
    val stone100: Color = Color(0xFFF5F5F4),
    val stone200: Color = Color(0xFFE7E5E4),
    val stone300: Color = Color(0xFFD6D3D1),
    val stone400: Color = Color(0xFFA8A29E),
    val stone500: Color = Color(0xFF79716B),
    val stone600: Color = Color(0xFF57534D),
    val stone800: Color = Color(0xFF292524),
    val stone900: Color = Color(0xFF1C1917),
    val stone950: Color = Color(0xFF0C0A09),
    val neutral50: Color = Color(0xFFFAFAFA),
    val neutral100: Color = Color(0xFFF5F5F5),
    val neutral200: Color = Color(0xFFE5E5E5),
    val neutral300: Color = Color(0xFFD4D4D4),
    val neutral400: Color = Color(0xFFA1A1A1),
    val neutral500: Color = Color(0xFF737373),
    val neutral600: Color = Color(0xFF525252),
    val neutral700: Color = Color(0xFF404040),
    val neutral800: Color = Color(0xFF262626),
    val neutral950: Color = Color(0xFF0A0A0A),
    val white: Color = Color(0xFFFFFFFF),
    val black: Color = Color(0xFF000000),
    val red500: Color = Color(0xFFFB2C36),
    val red700: Color = Color(0xFFB91C1C),
    val logo: Color = Color(0xFFCE2C2C),
    val primary700: Color = Color(0xFFB42318),
    val primary600: Color = Color(0xFFD92D20),
)

// text, bg, border: dung cho nhung truong hop ko phai common
data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val outline: Color,
    val error: Color,
    val onError: Color,
    val text: Colors,
    val bg: Colors,
    val border: Colors,
)

val LightAppColors = AppColors(
    primary = Colors().stone950,
    onPrimary = Colors().neutral50,
    secondary = Colors().stone100,
    onSecondary = Colors().neutral950,
    tertiary = Color.Green,
    onTertiary = Colors().neutral50,
    background = Colors().white,
    onBackground = Colors().neutral950,
    surface = Colors().white,
    onSurface = Colors().neutral950,
    outline = Color.Transparent,
    error = Colors().red500,
    onError = Color.White,
    text = Colors(),
    bg = Colors(),
    border = Colors(),
)

val DarkAppColors = AppColors(
    primary = Colors().primary600,
    onPrimary = Colors().white,
    secondary = Colors().stone800,
    onSecondary = Colors().neutral100,
    tertiary = Colors().stone300,
    onTertiary = Colors().stone950,
    background = Colors().stone950,
    onBackground = Colors().neutral100,
    surface = Colors().stone900,
    onSurface = Colors().neutral50,
    outline = Colors().stone600,
    error = Colors().red700,
    onError = Colors().white,
    text = Colors(),
    bg = Colors(),
    border = Colors(),
)

fun AppColors.toColorScheme() = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    secondary = secondary,
    onSecondary = onSecondary,
    tertiary = tertiary,
    onTertiary = onTertiary,
    background = background,
    onBackground = onBackground,
    surface = surface,
    onSurface = onSurface,
    outline = outline,
    error = error,
    onError = onError,
)

fun AppColors.toDarkColorScheme() = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    secondary = secondary,
    onSecondary = onSecondary,
    tertiary = tertiary,
    onTertiary = onTertiary,
    background = background,
    onBackground = onBackground,
    surface = surface,
    onSurface = onSurface,
    outline = outline,
    error = error,
    onError = onError
)
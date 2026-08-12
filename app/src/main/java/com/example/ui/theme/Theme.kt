package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BullishGreen,
    onPrimary = Color.Black,
    primaryContainer = BullishGreenContainer,
    onPrimaryContainer = Color.White,
    secondary = FastEmaCyan,
    onSecondary = Color.Black,
    secondaryContainer = NavySurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = SlowEmaYellow,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NavyCardBorder,
    error = BearishRed,
    errorContainer = BearishRedContainer,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme(
    primary = BullishGreen,
    onPrimary = Color.Black,
    primaryContainer = BullishGreenContainer,
    onPrimaryContainer = Color.White,
    secondary = FastEmaCyan,
    onSecondary = Color.Black,
    secondaryContainer = NavySurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = SlowEmaYellow,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NavyCardBorder,
    error = BearishRed,
    errorContainer = BearishRedContainer,
    onError = Color.White
)

@Composable
fun BinaryTrendBotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our high-contrast trading theme by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

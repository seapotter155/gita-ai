package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SaffronAccent,
    secondary = CalmTeal,
    tertiary = GoldDivider,
    background = CosmicBackground,
    surface = CosmicSurface,
    onPrimary = CosmicBackground,
    onSecondary = CosmicOnSurface,
    onBackground = CosmicOnBackground,
    onSurface = CosmicOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    secondary = CalmTeal,
    tertiary = GoldDivider,
    background = ParchmentBackground,
    surface = ParchmentSurface,
    onPrimary = ParchmentSurface,
    onSecondary = ParchmentOnSurface,
    onBackground = ParchmentOnBackground,
    onSurface = ParchmentOnSurface
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic coloring so our sacred Saffron spiritual identity is preserved
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

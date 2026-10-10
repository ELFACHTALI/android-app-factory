package com.elfachtali.bruitblanc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Toujours sombre : l'app sert surtout la nuit, dans une chambre peu éclairée.
private val NightColors = darkColorScheme(
    primary = Color(0xFF9FB4FF),
    onPrimary = Color(0xFF0E1A3D),
    primaryContainer = Color(0xFF26345F),
    onPrimaryContainer = Color(0xFFDCE2FF),
    background = Color(0xFF0B1020),
    onBackground = Color(0xFFE3E5F0),
    surface = Color(0xFF0B1020),
    onSurface = Color(0xFFE3E5F0),
    surfaceVariant = Color(0xFF1A2138),
    onSurfaceVariant = Color(0xFFB9BED3),
)

/** Thème Material 3 de l'app, toujours en couleurs de nuit. */
@Composable
fun BruitBlancTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NightColors, content = content)
}

package com.winlator.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class ThemePreset(
    val name: String,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val onSurface: Color = Color(0xFFE0E0E0),
    val onSurfaceVariant: Color = Color(0xFFAAAAAA),
) {
    fun toColorScheme(accentOverride: Color? = null): ColorScheme = darkColorScheme(
        primary          = accentOverride ?: primary,
        background       = background,
        surface          = surface,
        onSurface        = onSurface,
        surfaceVariant   = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        error            = Color(0xFFCF6679),
    )

    fun toLightColorScheme(accentOverride: Color? = null): ColorScheme = lightColorScheme(
        primary          = accentOverride ?: primary,
        onPrimary        = Color(0xFFFFFFFF),
        background       = Color(0xFFF5F5F5),
        onBackground     = Color(0xFF1A1A1A),
        surface          = Color(0xFFFFFFFF),
        onSurface        = Color(0xFF1A1A1A),
        surfaceVariant   = Color(0xFFEAEAEA),
        onSurfaceVariant = Color(0xFF555555),
        error            = Color(0xFFB00020),
    )
}

val themePresets = listOf(
    ThemePreset("Classic Dark", Color(0xFF1A1A1A), Color(0xFF2A2A2A), Color(0xFF333333), Color(0xFF8B6BE0)),
    ThemePreset("AMOLED",       Color(0xFF000000), Color(0xFF0D0D0D), Color(0xFF181818), Color(0xFFBB86FC)),
    ThemePreset("Ocean",        Color(0xFF0D1B2A), Color(0xFF162435), Color(0xFF1E3045), Color(0xFF0EA5E9)),
    ThemePreset("Forest",       Color(0xFF0D1A12), Color(0xFF142010), Color(0xFF1C2E1A), Color(0xFF22C55E)),
    ThemePreset("Sunset",       Color(0xFF1A0D0D), Color(0xFF251515), Color(0xFF301C1C), Color(0xFFF97316)),
    ThemePreset("Rose",         Color(0xFF1A0D14), Color(0xFF25151E), Color(0xFF301C28), Color(0xFFEC4899)),
    ThemePreset("Steel",        Color(0xFF131419), Color(0xFF1C1D25), Color(0xFF252630), Color(0xFF64748B)),
    ThemePreset("Custom",       Color(0xFF121212), Color(0xFF1E1E1E), Color(0xFF2A2A2A), Color(0xFF8B6BE0)),
)

val CUSTOM_PRESET_INDEX = themePresets.size - 1

package com.winlator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.winlator.ui.theme.AppThemeState
import com.winlator.ui.theme.CUSTOM_PRESET_INDEX
import com.winlator.ui.theme.themePresets

@Composable
fun AppearanceScreen() {
    val presetIndex  by AppThemeState.presetIndex.collectAsState()
    val isDarkMode   by AppThemeState.isDarkMode.collectAsState()
    val customAccent by AppThemeState.customAccent.collectAsState()
    val context      = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Theme Preset", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(themePresets.indices.toList()) { index ->
                val preset   = themePresets[index]
                val selected = presetIndex == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { AppThemeState.setPreset(index) }
                        .padding(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(preset.primary)
                            .then(
                                if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            ),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        preset.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (presetIndex == CUSTOM_PRESET_INDEX) {
            Spacer(Modifier.height(20.dp))
            Text("Accent Color", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            AccentColorPicker(
                currentColor    = customAccent,
                onColorSelected = { AppThemeState.setCustomAccent(it) },
            )
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Apply dark color scheme",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = { enabled ->
                    PreferenceManager.getDefaultSharedPreferences(context)
                        .edit().putBoolean("dark_mode", enabled).apply()
                },
            )
        }
    }
}

@Composable
private fun AccentColorPicker(currentColor: Color, onColorSelected: (Color) -> Unit) {
    val presetAccents = listOf(
        Color(0xFF8B6BE0), Color(0xFFBB86FC), Color(0xFF0EA5E9), Color(0xFF22C55E),
        Color(0xFFF97316), Color(0xFFEC4899), Color(0xFFEAB308), Color(0xFFEF4444),
        Color(0xFF64748B), Color(0xFF06B6D4), Color(0xFF10B981), Color(0xFFA855F7),
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(presetAccents) { color ->
            val selected = currentColor == color
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        else Modifier
                    )
                    .clickable { onColorSelected(color) },
            )
        }
    }
}

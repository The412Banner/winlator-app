package com.winlator.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.preference.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

object AppThemeState {
    private lateinit var themePrefs: SharedPreferences
    private lateinit var appPrefs: SharedPreferences
    // Held as a field — SharedPreferences stores listeners as WeakReference
    private var prefListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private val _presetIndex    = MutableStateFlow(0)
    private val _customAccent   = MutableStateFlow(Color(0xFF8B6BE0))
    private val _isDarkMode     = MutableStateFlow(true)
    private val _customBaseIndex = MutableStateFlow(0)

    val presetIndex:  StateFlow<Int>     = _presetIndex
    val customAccent: StateFlow<Color>   = _customAccent
    val isDarkMode:   StateFlow<Boolean> = _isDarkMode

    val colorScheme: Flow<ColorScheme> =
        combine(_presetIndex, _customAccent, _isDarkMode, _customBaseIndex) { index, accent, dark, baseIdx ->
            val preset = if (index == CUSTOM_PRESET_INDEX)
                themePresets.getOrElse(baseIdx) { themePresets.first() }
            else
                themePresets.getOrElse(index) { themePresets.first() }
            val override = if (index == CUSTOM_PRESET_INDEX) accent else null
            if (dark) preset.toColorScheme(accentOverride = override)
            else      preset.toLightColorScheme(accentOverride = override)
        }

    fun init(context: Context) {
        themePrefs = context.getSharedPreferences("winlator_theme", Context.MODE_PRIVATE)
        appPrefs   = PreferenceManager.getDefaultSharedPreferences(context)

        _presetIndex.value     = themePrefs.getInt("preset_index", 0).coerceIn(0, themePresets.size - 1)
        _customBaseIndex.value = themePrefs.getInt("custom_base_index", 0).coerceIn(0, themePresets.size - 1)
        _customAccent.value    = Color(themePrefs.getInt("custom_accent", Color(0xFF8B6BE0).toArgb()))
        _isDarkMode.value      = appPrefs.getBoolean("dark_mode", true)

        prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "dark_mode") _isDarkMode.value = appPrefs.getBoolean("dark_mode", true)
        }
        appPrefs.registerOnSharedPreferenceChangeListener(prefListener)
    }

    fun setPreset(index: Int) {
        _presetIndex.value = index.coerceIn(0, themePresets.size - 1)
        themePrefs.edit().putInt("preset_index", _presetIndex.value).apply()
    }

    fun setCustomAccent(color: Color) {
        if (_presetIndex.value != CUSTOM_PRESET_INDEX) {
            _customBaseIndex.value = _presetIndex.value
            themePrefs.edit().putInt("custom_base_index", _customBaseIndex.value).apply()
        }
        _customAccent.value = color
        _presetIndex.value  = CUSTOM_PRESET_INDEX
        themePrefs.edit()
            .putInt("custom_accent", color.toArgb())
            .putInt("preset_index", CUSTOM_PRESET_INDEX)
            .apply()
    }

    fun currentColorSchemeSnapshot(): ColorScheme {
        val index  = _presetIndex.value
        val baseIdx = _customBaseIndex.value
        val preset = if (index == CUSTOM_PRESET_INDEX)
            themePresets.getOrElse(baseIdx) { themePresets.first() }
        else
            themePresets.getOrElse(index) { themePresets.first() }
        val override = if (index == CUSTOM_PRESET_INDEX) _customAccent.value else null
        return if (_isDarkMode.value) preset.toColorScheme(accentOverride = override)
               else                   preset.toLightColorScheme(accentOverride = override)
    }
}

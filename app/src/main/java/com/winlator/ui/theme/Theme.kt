package com.winlator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun WinlatorTheme(content: @Composable () -> Unit) {
    val colorScheme by AppThemeState.colorScheme.collectAsState(
        initial = AppThemeState.currentColorSchemeSnapshot()
    )
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

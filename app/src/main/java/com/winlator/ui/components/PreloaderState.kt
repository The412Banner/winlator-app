package com.winlator.ui.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PreloaderState {
    private val _text = MutableStateFlow<String?>(null)
    val text: StateFlow<String?> = _text

    @JvmStatic fun show(t: String? = null) { _text.value = t ?: "" }
    @JvmStatic fun hide()                  { _text.value = null }
    @JvmStatic fun isVisible(): Boolean    = _text.value != null
}

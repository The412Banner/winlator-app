package com.winlator.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winlator.core.GeneralComponents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class ContentsViewModel(application: Application) : AndroidViewModel(application) {

    data class ComponentSection(
        val type: GeneralComponents.Type,
        val title: String,
        val installed: List<String>,
    )

    private val _sections = MutableStateFlow<List<ComponentSection>>(emptyList())
    val sections: StateFlow<List<ComponentSection>> = _sections

    init { refresh() }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val ctx = getApplication<Application>()
            _sections.value = GeneralComponents.Type.values().map { type ->
                ComponentSection(
                    type      = type,
                    title     = typeTitle(type),
                    installed = GeneralComponents.getInstalledComponentNames(type, ctx),
                )
            }
        }
    }

    fun remove(type: GeneralComponents.Type, identifier: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val ctx = getApplication<Application>()
            val dir = GeneralComponents.getComponentDir(type, ctx)
            dir.listFiles()?.forEach { file ->
                if (displayName(type, file.name) == identifier) file.deleteRecursively()
            }
            refresh()
        }
    }

    private fun displayName(type: GeneralComponents.Type, filename: String): String =
        filename
            .replace("${type.name.lowercase()}-", "")
            .replace(".tzst", "")
            .replace(".sf2", "")

    private fun typeTitle(type: GeneralComponents.Type) = when (type) {
        GeneralComponents.Type.BOX64            -> "Box64"
        GeneralComponents.Type.TURNIP           -> "Turnip"
        GeneralComponents.Type.DXVK             -> "DXVK"
        GeneralComponents.Type.VKD3D            -> "VKD3D"
        GeneralComponents.Type.WINED3D          -> "WineD3D"
        GeneralComponents.Type.SOUNDFONT        -> "SoundFont"
        GeneralComponents.Type.ADRENOTOOLS_DRIVER -> "AdrenoTools Driver"
    }
}

package com.winlator.ui.screens

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.winlator.XServerDisplayActivity
import com.winlator.container.ContainerManager
import com.winlator.container.Shortcut
import com.winlator.ui.components.PreloaderState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ShortcutSortOrder { NAME_ASC, NAME_DESC, CONTAINER }

class ShortcutsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    private val _shortcuts  = MutableStateFlow<List<Shortcut>>(emptyList())
    private val _sortOrder  = MutableStateFlow(
        ShortcutSortOrder.entries[
            prefs.getInt("shortcut_sort_order", ShortcutSortOrder.NAME_ASC.ordinal)
                .coerceIn(0, ShortcutSortOrder.entries.size - 1)
        ]
    )
    private val _isGridView = MutableStateFlow(prefs.getBoolean("shortcuts_grid_view", false))

    val sortOrder:  StateFlow<ShortcutSortOrder> = _sortOrder
    val isGridView: StateFlow<Boolean>           = _isGridView

    val shortcuts: Flow<List<Shortcut>> = combine(_shortcuts, _sortOrder) { list, order ->
        when (order) {
            ShortcutSortOrder.NAME_ASC  -> list.sortedBy { it.name.lowercase() }
            ShortcutSortOrder.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
            ShortcutSortOrder.CONTAINER -> list.sortedBy { (it.container?.name ?: "").lowercase() }
        }
    }

    init { refresh() }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val mgr = ContainerManager(getApplication())
            _shortcuts.value = mgr.loadShortcuts(null)
        }
    }

    fun setSortOrder(order: ShortcutSortOrder) {
        _sortOrder.value = order
        prefs.edit().putInt("shortcut_sort_order", order.ordinal).apply()
    }

    fun setGridView(grid: Boolean) {
        _isGridView.value = grid
        prefs.edit().putBoolean("shortcuts_grid_view", grid).apply()
    }

    fun remove(shortcut: Shortcut) {
        viewModelScope.launch(Dispatchers.IO) {
            shortcut.file.delete()
            refresh()
        }
    }

    fun runShortcut(shortcut: Shortcut, onLaunch: (Intent) -> Unit) {
        val intent = Intent(getApplication(), XServerDisplayActivity::class.java).apply {
            putExtra("container_id", shortcut.container.id)
            putExtra("shortcut_path", shortcut.file.path)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        onLaunch(intent)
    }
}

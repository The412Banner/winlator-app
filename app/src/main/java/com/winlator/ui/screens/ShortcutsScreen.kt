package com.winlator.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.winlator.R
import com.winlator.container.Shortcut
import com.winlator.ui.components.LocalTopBarActions

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ShortcutsScreen(
    navController: NavController,
    vm: ShortcutsViewModel = viewModel(),
) {
    val shortcuts  by vm.shortcuts.collectAsState(initial = emptyList())
    val isGridView by vm.isGridView.collectAsState()
    val sortOrder  by vm.sortOrder.collectAsState()
    val context    = LocalContext.current

    val topBarActions = LocalTopBarActions.current
    var showSortMenu by remember { mutableStateOf(false) }

    SideEffect {
        topBarActions.value = {
            // Sort button
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort")
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Name A→Z") },
                        onClick = { vm.setSortOrder(ShortcutSortOrder.NAME_ASC); showSortMenu = false },
                    )
                    DropdownMenuItem(
                        text = { Text("Name Z→A") },
                        onClick = { vm.setSortOrder(ShortcutSortOrder.NAME_DESC); showSortMenu = false },
                    )
                    DropdownMenuItem(
                        text = { Text("By Container") },
                        onClick = { vm.setSortOrder(ShortcutSortOrder.CONTAINER); showSortMenu = false },
                    )
                }
            }
            // Grid/list toggle
            IconButton(onClick = { vm.setGridView(!isGridView) }) {
                Icon(
                    if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = "Toggle layout",
                )
            }
        }
    }

    var confirmRemove by remember { mutableStateOf<Shortcut?>(null) }

    if (shortcuts.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No shortcuts yet.")
        }
    } else {
        AnimatedContent(targetState = isGridView, label = "layout") { grid ->
            if (grid) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(120.dp),
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                ) {
                    items(shortcuts, key = { it.file.path }) { shortcut ->
                        ShortcutGridItem(
                            shortcut  = shortcut,
                            onRun     = { vm.runShortcut(shortcut) { context.startActivity(it) } },
                            onRemove  = { confirmRemove = shortcut },
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(shortcuts, key = { it.file.path }) { shortcut ->
                        ShortcutListItem(
                            shortcut = shortcut,
                            onRun    = { vm.runShortcut(shortcut) { context.startActivity(it) } },
                            onRemove = { confirmRemove = shortcut },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    confirmRemove?.let { shortcut ->
        AlertDialog(
            onDismissRequest = { confirmRemove = null },
            title = { Text("Remove Shortcut") },
            text  = { Text("Remove \"${shortcut.name}\"?") },
            confirmButton = {
                TextButton(onClick = { vm.remove(shortcut); confirmRemove = null }) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemove = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ShortcutListItem(
    shortcut: Shortcut,
    onRun: () -> Unit,
    onRemove: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShortcutIcon(shortcut.icon, Modifier.size(48.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(shortcut.name, style = MaterialTheme.typography.bodyLarge)
            Text(shortcut.container?.name ?: "", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onRun) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = MaterialTheme.colorScheme.primary)
        }
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(text = { Text("Remove") }, onClick = { menuExpanded = false; onRemove() })
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ShortcutGridItem(
    shortcut: Shortcut,
    onRun: () -> Unit,
    onRemove: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(onLongClick = { menuExpanded = true }, onClick = onRun),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp),
        ) {
            ShortcutIcon(shortcut.icon, Modifier.size(72.dp))
            Text(
                shortcut.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(text = { Text("Run") }, onClick = { menuExpanded = false; onRun() })
            DropdownMenuItem(text = { Text("Remove") }, onClick = { menuExpanded = false; onRemove() })
        }
    }
}

@Composable
private fun ShortcutIcon(icon: Bitmap?, modifier: Modifier) {
    if (icon != null) {
        Image(
            bitmap = icon.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
        )
    } else {
        Icon(
            painter = painterResource(R.drawable.icon_container),
            contentDescription = null,
            modifier = modifier,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

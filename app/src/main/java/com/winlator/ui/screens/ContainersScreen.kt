package com.winlator.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.winlator.container.Container
import com.winlator.ui.components.LocalTopBarActions

@Composable
fun ContainersScreen(
    navController: NavController,
    vm: ContainersViewModel = viewModel(),
) {
    val containers by vm.containers.collectAsState()
    val context    = LocalContext.current
    val topBarActions = LocalTopBarActions.current

    SideEffect {
        topBarActions.value = {}
    }

    var confirmRemove by remember { mutableStateOf<Container?>(null) }
    var confirmDuplicate by remember { mutableStateOf<Container?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (containers.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No containers yet. Tap + to create one.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(containers, key = { it.id }) { container ->
                    ContainerItem(
                        container = container,
                        onRun = {
                            vm.runContainer(container) { intent ->
                                context.startActivity(intent)
                            }
                        },
                        onEdit = {
                            navController.navigate(Screen.ContainerDetail.route(container.id))
                        },
                        onFileManager = {
                            navController.navigate(
                                Screen.ContainerFileManager.route(container.id, container.rootDir?.path ?: "/")
                            )
                        },
                        onDuplicate = { confirmDuplicate = container },
                        onRemove    = { confirmRemove    = container },
                    )
                    HorizontalDivider()
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (vm.isRootFSValid()) navController.navigate(Screen.ContainerDetail.route(0))
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Container")
        }
    }

    confirmDuplicate?.let { container ->
        AlertDialog(
            onDismissRequest = { confirmDuplicate = null },
            title = { Text("Duplicate Container") },
            text  = { Text("Duplicate \"${container.name}\"?") },
            confirmButton = {
                TextButton(onClick = { vm.duplicate(container); confirmDuplicate = null }) {
                    Text("Duplicate")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDuplicate = null }) { Text("Cancel") }
            },
        )
    }

    confirmRemove?.let { container ->
        AlertDialog(
            onDismissRequest = { confirmRemove = null },
            title = { Text("Remove Container") },
            text  = { Text("Remove \"${container.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { vm.remove(container); confirmRemove = null }) {
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
private fun ContainerItem(
    container: Container,
    onRun: () -> Unit,
    onEdit: () -> Unit,
    onFileManager: () -> Unit,
    onDuplicate: () -> Unit,
    onRemove: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(container.name, style = MaterialTheme.typography.bodyLarge)
        }
        IconButton(onClick = onRun) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = MaterialTheme.colorScheme.primary)
        }
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(text = { Text("Edit") }, onClick = { menuExpanded = false; onEdit() })
                DropdownMenuItem(text = { Text("File Manager") }, onClick = { menuExpanded = false; onFileManager() })
                DropdownMenuItem(text = { Text("Duplicate") }, onClick = { menuExpanded = false; onDuplicate() })
                DropdownMenuItem(text = { Text("Remove") }, onClick = { menuExpanded = false; onRemove() })
            }
        }
    }
}

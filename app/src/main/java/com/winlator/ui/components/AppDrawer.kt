package com.winlator.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.winlator.BuildConfig
import com.winlator.ui.screens.Screen

@Composable
fun AppDrawer(
    navController: NavController,
    currentRoute: String?,
    onClose: () -> Unit,
) {
    var showAbout by remember { mutableStateOf(false) }

    ModalDrawerSheet {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Winlator",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        DrawerItem(Screen.Shortcuts,     "Shortcuts",      Icons.Default.SportsEsports, navController, currentRoute, onClose)
        DrawerItem(Screen.Containers,    "Containers",     Icons.Default.Storage,       navController, currentRoute, onClose)
        DrawerItem(Screen.InputControls, "Input Controls", Icons.Default.SportsEsports, navController, currentRoute, onClose)
        DrawerItem(Screen.Settings,      "Settings",       Icons.Default.Settings,      navController, currentRoute, onClose)

        NavigationDrawerItem(
            label    = { Text("About") },
            selected = false,
            icon     = { Icon(Icons.Default.Info, contentDescription = null) },
            onClick  = { onClose(); showAbout = true },
        )

        Spacer(Modifier.height(16.dp))
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About") },
            text  = {
                Text("Winlator ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n\nAndroid application for running Windows programs.")
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("OK") } },
        )
    }
}

@Composable
private fun DrawerItem(
    screen: Screen,
    label: String,
    icon: ImageVector,
    navController: NavController,
    currentRoute: String?,
    onClose: () -> Unit,
) {
    NavigationDrawerItem(
        label    = { Text(label) },
        selected = currentRoute == screen.route,
        icon     = { Icon(icon, contentDescription = null) },
        onClick  = {
            onClose()
            if (currentRoute != screen.route) {
                navController.navigate(screen.route) {
                    launchSingleTop = true
                    restoreState    = true
                }
            }
        },
    )
}

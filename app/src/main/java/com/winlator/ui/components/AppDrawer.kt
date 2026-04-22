package com.winlator.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.winlator.BuildConfig
import com.winlator.ui.screens.Screen

private const val GITHUB_URL = "https://github.com/brunodev85/winlator"

@Composable
fun AppDrawer(
    navController: NavController,
    currentRoute: String?,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var showAbout by remember { mutableStateOf(false) }
    var showHelp  by remember { mutableStateOf(false) }

    ModalDrawerSheet {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Winlator",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        DrawerItem(Screen.Containers, "Containers", Icons.Default.Storage, navController, currentRoute, onClose)
        DrawerItem(Screen.Shortcuts,  "Shortcuts",  Icons.Default.SportsEsports, navController, currentRoute, onClose)
        DrawerItem(Screen.Contents,   "Contents",   Icons.Default.FolderOpen, navController, currentRoute, onClose)
        DrawerItem(Screen.Saves,      "Saves",      Icons.Default.GridView, navController, currentRoute, onClose)

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        DrawerItem(Screen.AdrenoTools,   "AdrenoTools",    Icons.Default.Tune, navController, currentRoute, onClose)
        DrawerItem(Screen.InputControls, "Input Controls", Icons.Default.SportsEsports, navController, currentRoute, onClose)
        DrawerItem(Screen.Appearance,    "Appearance",     Icons.Default.Palette, navController, currentRoute, onClose)
        DrawerItem(Screen.Settings,      "Settings",       Icons.Default.Settings, navController, currentRoute, onClose)

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            label = { Text("Help & Support") },
            selected = false,
            icon = { Icon(Icons.Default.Help, contentDescription = null) },
            onClick = { onClose(); showHelp = true },
        )
        NavigationDrawerItem(
            label = { Text("About") },
            selected = false,
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            onClick = { onClose(); showAbout = true },
        )

        Spacer(Modifier.height(16.dp))
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Help & Support") },
            text = { Text("Report issues at the GitHub repository.") },
            confirmButton = {
                TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
                    showHelp = false
                }) { Text("Open GitHub") }
            },
            dismissButton = { TextButton(onClick = { showHelp = false }) { Text("Close") } },
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About") },
            text = {
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
        label = { Text(label) },
        selected = currentRoute == screen.route,
        icon = { Icon(icon, contentDescription = null) },
        onClick = {
            onClose()
            if (currentRoute != screen.route) {
                navController.navigate(screen.route) {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
    )
}

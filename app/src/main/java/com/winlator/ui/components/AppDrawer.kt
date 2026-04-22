package com.winlator.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.winlator.BuildConfig
import com.winlator.R
import com.winlator.ui.screens.Screen

@Composable
fun AppDrawer(
    navController: NavController,
    currentRoute: String?,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var showAbout by remember { mutableStateOf(false) }

    ModalDrawerSheet {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.app_name),
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
        AboutDialog(
            versionName = BuildConfig.VERSION_NAME,
            onDismiss   = { showAbout = false },
            onOpenUrl   = { url ->
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            },
        )
    }
}

@Composable
private fun AboutDialog(
    versionName: String,
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val linkColor = MaterialTheme.colorScheme.primary

    val credits = listOf(
        "GLIBC Patches by " to ("Termux Pacman" to "https://github.com/termux-pacman/glibc-packages"),
        "Wine " to ("winehq.org" to "https://www.winehq.org"),
        "Box86/Box64 by " to ("ptitseb" to "https://github.com/ptitSeb"),
        "Mesa (Turnip/Zink/VirGL) " to ("mesa3d.org" to "https://www.mesa3d.org"),
        "DXVK " to ("github.com/doitsujin/dxvk" to "https://github.com/doitsujin/dxvk"),
        "VKD3D " to ("gitlab.winehq.org/wine/vkd3d" to "https://gitlab.winehq.org/wine/vkd3d"),
        "CNC DDraw " to ("github.com/FunkyFr3sh/cnc-ddraw" to "https://github.com/FunkyFr3sh/cnc-ddraw"),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
        text = {
            Column {
                // Header row: name + version on left, icon on right
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        val websiteText = buildAnnotatedString {
                            pushStringAnnotation("URL", "https://www.winlator.org")
                            withStyle(SpanStyle(color = linkColor)) { append("winlator.org") }
                            pop()
                        }
                        ClickableText(
                            text  = websiteText,
                            style = MaterialTheme.typography.bodyMedium,
                            onClick = { offset ->
                                websiteText.getStringAnnotations("URL", offset, offset)
                                    .firstOrNull()?.let { onOpenUrl(it.item) }
                            },
                        )
                        Text(
                            text  = "${stringResource(R.string.version)} $versionName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher),
                        contentDescription = null,
                        modifier = Modifier.size(70.dp),
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text  = stringResource(R.string.credits_and_third_party_apps),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))

                credits.forEach { (prefix, linkPair) ->
                    val (linkText, url) = linkPair
                    val annotated = buildAnnotatedString {
                        append(prefix)
                        pushStringAnnotation("URL", url)
                        withStyle(SpanStyle(color = linkColor)) { append(linkText) }
                        pop()
                    }
                    ClickableText(
                        text  = annotated,
                        style = MaterialTheme.typography.bodySmall,
                        onClick = { offset ->
                            annotated.getStringAnnotations("URL", offset, offset)
                                .firstOrNull()?.let { onOpenUrl(it.item) }
                        },
                    )
                }
            }
        },
    )
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

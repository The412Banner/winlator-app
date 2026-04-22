package com.winlator.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.winlator.InputControlsFragment
import com.winlator.SettingsFragment
import com.winlator.ui.components.AppDrawer
import com.winlator.ui.components.AppTopBar
import com.winlator.ui.components.FragmentScreen
import com.winlator.ui.components.LocalTopBarActions
import com.winlator.ui.components.PreloaderOverlay
import com.winlator.ui.components.topBarActionsState
import kotlinx.coroutines.launch
import java.net.URLDecoder

@Composable
fun AppNavGraph(startScreen: String? = null) {
    val navController      = rememberNavController()
    val drawerState        = rememberDrawerState(DrawerValue.Closed)
    val scope              = rememberCoroutineScope()
    val topBarActionsState = remember { topBarActionsState() }

    val backStack    by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    LaunchedEffect(currentRoute) { topBarActionsState.value = {} }

    LaunchedEffect(startScreen) {
        if (startScreen != null) navController.navigate(startScreen)
    }

    val title = when {
        currentRoute == Screen.Containers.route    -> "Containers"
        currentRoute == Screen.Shortcuts.route     -> "Shortcuts"
        currentRoute == Screen.InputControls.route -> "Input Controls"
        currentRoute == Screen.Settings.route      -> "Settings"
        currentRoute?.startsWith("container_detail") == true       -> "Container"
        currentRoute?.startsWith("container_file_manager") == true -> "File Manager"
        else -> "Winlator"
    }

    CompositionLocalProvider(LocalTopBarActions provides topBarActionsState) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawer(
                    navController = navController,
                    currentRoute  = currentRoute,
                    onClose       = { scope.launch { drawerState.close() } },
                )
            },
        ) {
            Scaffold(
                topBar = {
                    AppTopBar(
                        title       = title,
                        onMenuClick = { scope.launch { drawerState.open() } },
                    )
                },
            ) { _ ->
                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController    = navController,
                        startDestination = Screen.Shortcuts.route,
                        modifier         = Modifier.fillMaxSize(),
                    ) {
                        composable(Screen.Shortcuts.route) {
                            ShortcutsScreen(navController)
                        }
                        composable(Screen.Containers.route) {
                            ContainersScreen(navController)
                        }
                        composable(Screen.InputControls.route) {
                            FragmentScreen(InputControlsFragment::class.java)
                        }
                        composable(Screen.Settings.route) {
                            FragmentScreen(SettingsFragment::class.java)
                        }
                        composable(
                            route     = Screen.ContainerDetail.route,
                            arguments = listOf(navArgument("containerId") { type = NavType.IntType }),
                        ) { backStack ->
                            val id = backStack.arguments?.getInt("containerId") ?: return@composable
                            ContainerDetailScreen(containerId = id)
                        }
                        composable(
                            route     = Screen.ContainerFileManager.route,
                            arguments = listOf(
                                navArgument("containerId") { type = NavType.IntType },
                                navArgument("startPath")   { type = NavType.StringType },
                            ),
                        ) { backStack ->
                            val containerId = backStack.arguments?.getInt("containerId") ?: return@composable
                            val startPath   = URLDecoder.decode(
                                backStack.arguments?.getString("startPath") ?: "", "UTF-8"
                            )
                            ContainerFileManagerScreen(containerId = containerId, startPath = startPath)
                        }
                    }
                    PreloaderOverlay()
                }
            }
        }
    }
}

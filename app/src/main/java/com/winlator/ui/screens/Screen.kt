package com.winlator.ui.screens

sealed class Screen(val route: String) {
    object Shortcuts     : Screen("shortcuts")
    object Containers    : Screen("containers")
    object InputControls : Screen("input_controls")
    object Settings      : Screen("settings")
    object ContainerDetail : Screen("container_detail/{containerId}") {
        fun route(id: Int) = "container_detail/$id"
    }
    object ContainerFileManager : Screen("container_file_manager/{containerId}/{startPath}") {
        fun route(containerId: Int, startPath: String) =
            "container_file_manager/$containerId/${java.net.URLEncoder.encode(startPath, "UTF-8")}"
    }
}

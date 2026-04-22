package com.winlator.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.winlator.ContainerFileManagerFragment
import com.winlator.ui.components.FragmentScreen

@Composable
fun ContainerFileManagerScreen(containerId: Int, startPath: String) {
    val fragment = remember(containerId, startPath) {
        ContainerFileManagerFragment(containerId, startPath)
    }
    FragmentScreen(fragment = fragment, modifier = Modifier.fillMaxSize())
}

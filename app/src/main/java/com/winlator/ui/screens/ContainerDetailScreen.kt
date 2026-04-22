package com.winlator.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.winlator.ContainerDetailFragment
import com.winlator.ui.components.FragmentScreen

@Composable
fun ContainerDetailScreen(containerId: Int) {
    val fragment = remember(containerId) { ContainerDetailFragment(containerId) }
    FragmentScreen(fragment = fragment, modifier = Modifier.fillMaxSize())
}

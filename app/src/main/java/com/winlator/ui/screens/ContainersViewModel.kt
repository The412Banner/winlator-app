package com.winlator.ui.screens

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winlator.XServerDisplayActivity
import com.winlator.container.Container
import com.winlator.container.ContainerManager
import com.winlator.ui.components.PreloaderState
import com.winlator.xenvironment.RootFS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContainersViewModel(application: Application) : AndroidViewModel(application) {
    private val _containers = MutableStateFlow<List<Container>>(emptyList())
    val containers: StateFlow<List<Container>> = _containers

    init { refresh() }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val mgr = ContainerManager(getApplication())
            _containers.value = mgr.getContainers()
        }
    }

    fun duplicate(container: Container) {
        viewModelScope.launch(Dispatchers.IO) {
            PreloaderState.show()
            val mgr = ContainerManager(getApplication())
            mgr.duplicateContainerAsync(container) {
                viewModelScope.launch {
                    refresh()
                    PreloaderState.hide()
                }
            }
        }
    }

    fun remove(container: Container) {
        viewModelScope.launch(Dispatchers.IO) {
            PreloaderState.show()
            val mgr = ContainerManager(getApplication())
            mgr.removeContainerAsync(container) {
                viewModelScope.launch {
                    refresh()
                    PreloaderState.hide()
                }
            }
        }
    }

    fun isRootFSValid(): Boolean = RootFS.find(getApplication()).isValid()

    fun runContainer(container: Container, onLaunch: (Intent) -> Unit) {
        val intent = Intent(getApplication(), XServerDisplayActivity::class.java).apply {
            putExtra("container_id", container.id)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        onLaunch(intent)
    }
}

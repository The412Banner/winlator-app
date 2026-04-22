package com.winlator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.winlator.core.Callback
import com.winlator.core.LocaleHelper
import com.winlator.ui.components.PreloaderState
import com.winlator.ui.screens.AppNavGraph
import com.winlator.ui.theme.AppThemeState
import com.winlator.ui.theme.WinlatorTheme
import com.winlator.xenvironment.RootFSInstaller

class MainActivity : AppCompatActivity() {

    companion object {
        @JvmField val DEBUG_MODE = false
        @JvmField val CONTAINER_PATTERN_COMPRESSION_LEVEL: Byte = 9
        const val PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE: Int = 1
        const val OPEN_FILE_REQUEST_CODE: Int = 2
        const val EDIT_INPUT_CONTROLS_REQUEST_CODE: Int = 3
        const val OPEN_DIRECTORY_REQUEST_CODE: Int = 4
    }

    // Compat shim: InputControlsFragment still calls preloaderDialog.show(resId) / .close()
    @JvmField val preloaderDialog = object {
        fun show(resId: Int) = PreloaderState.show(this@MainActivity.getString(resId))
        fun close() = PreloaderState.hide()
    }

    private var openFileCallback: Callback<Uri>? = null

    fun setOpenFileCallback(callback: Callback<Uri>) {
        openFileCallback = callback
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setSystemLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppThemeState.init(this)
        super.onCreate(savedInstanceState)

        val editInputControls = intent.getBooleanExtra("edit_input_controls", false)
        val startScreen = when {
            editInputControls -> "input_controls"
            else -> null
        }

        if (!requestAppPermissions()) {
            RootFSInstaller.installIfNeeded(this)
        }

        setContent {
            WinlatorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(startScreen = startScreen)
                }
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { openFileCallback?.call(it) }
            openFileCallback = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                RootFSInstaller.installIfNeeded(this)
            } else {
                finish()
            }
        }
    }

    private fun requestAppPermissions(): Boolean {
        val writeGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val readGranted  = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (writeGranted && readGranted) return false
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE,
        )
        return true
    }
}

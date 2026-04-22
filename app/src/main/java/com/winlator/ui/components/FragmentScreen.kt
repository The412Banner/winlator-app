package com.winlator.ui.components

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.winlator.R

@Composable
fun FragmentScreen(
    fragmentClass: Class<out Fragment>,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    val context = LocalContext.current
    val fragmentManager = (context as FragmentActivity).supportFragmentManager
    AndroidView(
        factory = { ctx ->
            val themedCtx = ContextThemeWrapper(ctx, R.style.AppThemeDark)
            FragmentContainerView(themedCtx).apply {
                id = View.generateViewId()
            }
        },
        update = { view ->
            if (fragmentManager.findFragmentById(view.id) == null) {
                fragmentManager.beginTransaction()
                    .replace(view.id, fragmentClass.getDeclaredConstructor().newInstance())
                    .commitNow()
            }
        },
        modifier = modifier,
    )
}

@Composable
fun FragmentScreen(
    fragment: Fragment,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    val context = LocalContext.current
    val fragmentManager = (context as FragmentActivity).supportFragmentManager
    AndroidView(
        factory = { ctx ->
            val themedCtx = ContextThemeWrapper(ctx, R.style.AppThemeDark)
            FragmentContainerView(themedCtx).apply {
                id = View.generateViewId()
            }
        },
        update = { view ->
            val existing = fragmentManager.findFragmentById(view.id)
            if (existing == null || existing !== fragment) {
                fragmentManager.beginTransaction()
                    .replace(view.id, fragment)
                    .commitNow()
            }
        },
        modifier = modifier,
    )
}

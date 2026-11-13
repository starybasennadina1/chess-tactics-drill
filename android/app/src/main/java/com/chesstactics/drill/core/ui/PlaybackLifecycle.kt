package com.chesstactics.drill.core.ui

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun PlaybackLifecycle(key: Any, onResume: () -> Unit, onPause: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    val resume by rememberUpdatedState(onResume)
    val pause by rememberUpdatedState(onPause)
    DisposableEffect(owner, key) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> resume()
                Lifecycle.Event.ON_STOP -> pause()
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) resume()
        onDispose { owner.lifecycle.removeObserver(observer); pause() }
    }
}

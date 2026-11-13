package com.chesstactics.drill.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstactics.drill.core.config.GameConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SplashUiState(val finished: Boolean = false, val acknowledged: Boolean = false)

class SplashViewModel : ViewModel() {
    private val _state = MutableStateFlow(SplashUiState())
    val state = _state.asStateFlow()
    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()
    init {
        viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            _finished.value = true
            _state.update { it.copy(finished = true) }
        }
    }
    fun acknowledge(): Boolean {
        if (!_state.value.finished || _state.value.acknowledged) return false
        _state.update { it.copy(acknowledged = true) }
        return true
    }
}

package com.chesstactics.drill.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstactics.drill.core.navigation.Screen
import com.chesstactics.drill.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(val history: List<Screen> = emptyList(), val reducedMotion: Boolean = false, val generation: Int = 0)

class AppViewModel(private val progress: ProgressRepository, private val systemReducedMotion: Boolean) : ViewModel() {
    private val _screen = MutableStateFlow<Screen>(Screen.Splash)
    val screen = _screen.asStateFlow()
    private val _state = MutableStateFlow(AppUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch { progress.load() }
        viewModelScope.launch { progress.state.collect { store -> _state.update { it.copy(reducedMotion = systemReducedMotion || store.progress.preferences.reducedMotion) } } }
    }

    fun splashFinished() { if (_screen.value == Screen.Splash) _screen.value = Screen.Menu }
    fun navigate(screen: Screen) {
        if (screen == _screen.value || _screen.value == Screen.Splash) return
        _state.update { it.copy(history = it.history + _screen.value) }
        _screen.value = screen
    }
    fun back() {
        val previous = _state.value.history.lastOrNull() ?: return
        _state.update { it.copy(history = it.history.dropLast(1)) }
        _screen.value = previous
    }
    fun menu() {
        _state.update { it.copy(history = emptyList()) }
        _screen.value = Screen.Menu
    }
    fun replay(sessionId: String) {
        _state.update { it.copy(history = listOf(Screen.Menu)) }
        _screen.value = Screen.Game(sessionId)
    }
    fun progressReset() {
        _state.update { it.copy(history = emptyList(), generation = it.generation + 1) }
        _screen.value = Screen.Menu
    }
}

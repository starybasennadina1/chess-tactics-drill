package com.chesstactics.drill.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.domain.repository.ProgressRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(val preferences: Preferences = Preferences(), val saving: Boolean = false, val resetDialog: Boolean = false, val error: Boolean = false, val resetPending: Boolean = false)
class SettingsViewModel(private val progress: ProgressRepository) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState(preferences = progress.state.value.progress.preferences))
    val state = _state.asStateFlow()
    private val _events = Channel<Unit>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    fun preferences(preferences: Preferences) {
        if (_state.value.saving) return
        _state.update { it.copy(preferences = preferences, saving = true, error = false, resetPending = false) }
        viewModelScope.launch {
            val saved = progress.setPreferences(preferences)
            _state.update { it.copy(saving = false, error = !saved) }
        }
    }
    fun openReset() { _state.update { it.copy(resetDialog = true) } }
    fun closeReset() { _state.update { it.copy(resetDialog = false) } }
    fun reset() {
        if (_state.value.saving) return
        _state.update { it.copy(saving = true, resetDialog = false, error = false, resetPending = true) }
        viewModelScope.launch {
            val saved = progress.reset()
            _state.update { it.copy(saving = false, error = !saved, resetPending = !saved) }
            if (saved) _events.send(Unit)
        }
    }
    fun retry() { if (_state.value.resetPending) reset() else preferences(_state.value.preferences) }
}

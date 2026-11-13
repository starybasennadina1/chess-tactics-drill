package com.chesstactics.drill.presentation.gameover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.domain.repository.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GameOverUiState(val puzzle: Puzzle? = null, val session: Session? = null, val result: AttemptResult? = null, val saving: Boolean = true, val error: Boolean = false, val starting: Boolean = false)
class GameOverViewModel(private val id: String, library: PuzzleRepository, private val progress: ProgressRepository) : ViewModel() {
    private val _state = MutableStateFlow(GameOverUiState())
    val state = _state.asStateFlow()
    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    init {
        val session = progress.session(id)
        _state.update { it.copy(session = session, puzzle = session?.let { item -> library.find(item.puzzleId) }) }
        retrySave()
        viewModelScope.launch {
            progress.state.collect { store ->
                store.progress.results.find { it.session.id == id }?.let { result -> _state.update { it.copy(result = result, saving = false, error = false) } }
            }
        }
    }
    fun retrySave() {
        val session = _state.value.session
        if (session == null) { _state.update { it.copy(saving = false, error = true) }; return }
        _state.update { it.copy(saving = true, error = false) }
        viewModelScope.launch {
            val result = progress.finalize(session)
            _state.update { it.copy(result = result, saving = false, error = result == null) }
        }
    }
    fun playAgain() {
        val puzzle = _state.value.puzzle ?: return
        if (_state.value.saving || _state.value.result == null || _state.value.starting) return
        _state.update { it.copy(starting = true) }
        viewModelScope.launch {
            val session = progress.createSession(puzzle.id)
            _state.update { it.copy(starting = false, error = session == null) }
            session?.let { _events.send(it.id) }
        }
    }
}

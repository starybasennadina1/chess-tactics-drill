package com.chesstactics.drill.presentation.rating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.domain.repository.*
import com.chesstactics.drill.domain.usecase.SelectPuzzleUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RatingUiState(val loading: Boolean = true, val progress: Progress = Progress(), val puzzles: List<Puzzle> = emptyList(), val error: Boolean = false, val busy: Boolean = false)
class RatingViewModel(private val library: PuzzleRepository, private val progress: ProgressRepository) : ViewModel() {
    private val _state = MutableStateFlow(RatingUiState(puzzles = library.puzzles))
    val state = _state.asStateFlow()
    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    init { viewModelScope.launch { progress.state.collect { store -> _state.update { it.copy(loading = store.loading, progress = store.progress, error = store.error) } } } }
    fun retry() {
        viewModelScope.launch {
            if (!progress.state.value.ready) progress.load() else {
                progress.setFilters(progress.state.value.progress.filters)
                progress.pending.value.forEach { progress.finalize(it) }
            }
        }
    }
    fun play() {
        if (_state.value.busy || _state.value.loading) return
        val data = progress.state.value.progress
        val puzzle = SelectPuzzleUseCase().eligible(library.puzzles, data, data.filters).firstOrNull()
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            val session = puzzle?.let { progress.createSession(it.id) }
            _state.update { it.copy(busy = false) }
            _events.send(session?.id.orEmpty())
        }
    }
}

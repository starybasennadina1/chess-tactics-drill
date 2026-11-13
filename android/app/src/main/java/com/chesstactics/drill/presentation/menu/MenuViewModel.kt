package com.chesstactics.drill.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.domain.repository.*
import com.chesstactics.drill.domain.usecase.SelectPuzzleUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MenuViewModel(private val library: PuzzleRepository, private val progress: ProgressRepository) : ViewModel() {
    private val _state = MutableStateFlow(MenuUiState())
    val state = _state.asStateFlow()
    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var chosen: Filters? = null
    private val selector = SelectPuzzleUseCase()

    init {
        viewModelScope.launch {
            combine(progress.state, progress.pending) { store, pending -> store to pending }.collect { (store, pending) ->
                val data = store.progress
                val filters = chosen ?: data.filters
                val eligible = selector.eligible(library.puzzles, data, filters)
                _state.update { it.copy(loading = store.loading, ready = store.ready, filters = filters, rating = data.rating, bestStreak = data.bestStreak, puzzles = library.puzzles,
                    solved = data.puzzles.filterValues { detail -> detail.solved }.keys, eligible = eligible, resume = data.session?.takeIf { session -> session.phase != Phase.COMPLETED },
                    error = store.error, contentError = library.invalidCount > 0, corruptSession = store.corruptSession, pendingCount = pending.size) }
            }
        }
    }

    fun filters(filters: Filters) {
        chosen = filters
        val data = progress.state.value.progress
        val eligible = selector.eligible(library.puzzles, data, filters)
        _state.update { it.copy(filters = filters, eligible = eligible) }
        viewModelScope.launch { if (progress.setFilters(filters) && chosen == filters) chosen = null }
    }
    fun play() {
        if (_state.value.busy || _state.value.pendingCount > 0 || !progress.state.value.ready) return
        val puzzle = _state.value.eligible.firstOrNull() ?: return
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            val session = progress.createSession(puzzle.id)
            _state.update { it.copy(busy = false, error = session == null) }
            session?.let { _events.send(it.id) }
        }
    }
    fun resume() { _state.value.resume?.let { viewModelScope.launch { _events.send(it.id) } } }
    fun retry() {
        viewModelScope.launch {
            if (!progress.state.value.ready) progress.load()
            if (progress.state.value.ready) {
                if (progress.setFilters(_state.value.filters)) chosen = null
                progress.state.value.progress.session?.let { active -> progress.session(active.id)?.let { progress.saveSession(it) } }
            }
            progress.pending.value.forEach { progress.finalize(it) }
        }
    }
}

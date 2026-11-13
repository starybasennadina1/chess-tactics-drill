package com.chesstactics.drill.presentation.solution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstactics.drill.core.config.GameConfig
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.domain.repository.*
import com.chesstactics.drill.domain.rules.ChessRules
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SolutionUiState(val puzzle: Puzzle? = null, val snapshots: List<Board> = emptyList(), val index: Int = 0, val playing: Boolean = false, val orientation: PieceColor = PieceColor.WHITE, val preferences: Preferences = Preferences(), val error: Boolean = false) {
    val board: Board? get() = snapshots.getOrNull(index)
}
class SolutionViewModel(id: String, library: PuzzleRepository, progress: ProgressRepository) : ViewModel() {
    private val _state = MutableStateFlow(SolutionUiState())
    val state = _state.asStateFlow()
    private var job: Job? = null
    private var resumeIntent = false
    init {
        val session = progress.session(id)?.takeIf { it.phase == Phase.COMPLETED }
        val puzzle = session?.let { library.find(it.puzzleId) }
        if (puzzle != null) {
            val snapshots = runCatching { (0..puzzle.line.size).map { ChessRules.replay(puzzle, puzzle.line.take(it)) } }.getOrNull()
            _state.update { it.copy(puzzle = puzzle, snapshots = snapshots.orEmpty(), orientation = ChessRules.parseFen(puzzle.fen).turn, error = snapshots == null) }
        } else _state.update { it.copy(error = true) }
        viewModelScope.launch { progress.state.collect { store -> _state.update { it.copy(preferences = store.progress.preferences) } } }
    }
    fun step(amount: Int) {
        pause()
        _state.update { it.copy(index = (it.index + amount).coerceIn(0, maxOf(0, it.snapshots.lastIndex))) }
    }
    fun restart() { pause(); _state.update { it.copy(index = 0) } }
    fun autoplay() {
        if (_state.value.playing) { pause(); return }
        if (_state.value.index >= _state.value.snapshots.lastIndex) return
        job?.cancel()
        resumeIntent = true
        _state.update { it.copy(playing = true) }
        job = viewModelScope.launch {
            while (isActive && _state.value.index < _state.value.snapshots.lastIndex) {
                delay(GameConfig.REPLAY_DELAY_MS)
                _state.update { it.copy(index = it.index + 1) }
            }
            resumeIntent = false
            _state.update { it.copy(playing = false) }
        }
    }
    fun pause(resumeLater: Boolean = false) {
        resumeIntent = resumeLater && (resumeIntent || _state.value.playing)
        job?.cancel()
        job = null
        _state.update { it.copy(playing = false) }
    }
    fun resume() { if (resumeIntent) autoplay() }
}

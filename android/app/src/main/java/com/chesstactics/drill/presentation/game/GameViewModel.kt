package com.chesstactics.drill.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstactics.drill.core.audio.SoundFeedback
import com.chesstactics.drill.core.config.GameConfig
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.domain.repository.*
import com.chesstactics.drill.domain.rules.ChessRules
import com.chesstactics.drill.domain.usecase.EvaluateMoveUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameViewModel(private val id: String, library: PuzzleRepository, private val progress: ProgressRepository, private val sound: SoundFeedback) : ViewModel() {
    private val _state = MutableStateFlow(GameUiState())
    val state = _state.asStateFlow()
    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private val evaluator = EvaluateMoveUseCase()
    private var job: Job? = null
    private var routed = false

    init {
        val session = progress.session(id)
        val puzzle = session?.let { library.find(it.puzzleId) }
        if (session != null && puzzle != null) {
            val board = runCatching { ChessRules.replay(puzzle, session.plies) }.getOrNull()
            _state.update { it.copy(puzzle = puzzle, session = session, board = board, orientation = ChessRules.parseFen(puzzle.fen).turn, status = if (board == null) GameStatus.ERROR else if (session.phase == Phase.COMPLETED) GameStatus.COMPLETE else GameStatus.SELECT) }
            render(session)
        } else _state.update { it.copy(status = GameStatus.ERROR) }
        viewModelScope.launch { progress.state.collect { store -> _state.update { it.copy(preferences = store.progress.preferences) } } }
    }

    private fun render(session: Session) {
        val puzzle = _state.value.puzzle ?: return
        val board = runCatching { ChessRules.replay(puzzle, session.plies) }.getOrNull() ?: return
        val moves = if (session.phase == Phase.PLAYER) ChessRules.legalMoves(board) else emptyList()
        _state.update { it.copy(session = session, board = board, moves = moves, hintSquare = if (session.hintUsed && session.phase == Phase.PLAYER) puzzle.line.getOrNull(session.plies.size)?.let { uci -> Move.parse(uci).from } else null) }
    }
    private fun persist() {
        val session = _state.value.session ?: return
        viewModelScope.launch { val saved = progress.saveSession(session); _state.update { it.copy(storageError = !saved) } }
    }
    fun openChooser(chooser: Chooser) {
        if (!_state.value.canInput || (chooser == Chooser.DESTINATION && _state.value.session?.source == null)) return
        _state.update { it.copy(chooser = chooser) }
    }
    fun closeOverlay() {
        if (_state.value.chooser == Chooser.PROMOTION) {
            _state.value.session?.let { render(it.copy(destination = null, promotion = null)) }
            _state.update { it.copy(status = GameStatus.DESTINATION) }
            persist()
        }
        _state.update { it.copy(chooser = Chooser.NONE, hintVisible = false) }
    }
    fun chooseSource(source: Int) {
        if (!_state.value.canInput || _state.value.moves.none { it.from == source }) return
        val session = _state.value.session ?: return
        render(session.copy(source = source, destination = null, promotion = null))
        _state.update { it.copy(chooser = Chooser.NONE, status = GameStatus.DESTINATION) }
        persist()
    }
    fun chooseDestination(destination: Int) {
        if (!_state.value.canInput) return
        val session = _state.value.session ?: return
        val choices = _state.value.moves.filter { it.from == session.source && it.to == destination }
        if (choices.isEmpty()) return
        render(session.copy(destination = destination, promotion = null))
        _state.update { it.copy(chooser = if (choices.any { move -> move.promotion != null }) Chooser.PROMOTION else Chooser.NONE, status = GameStatus.READY) }
        persist()
    }
    fun choosePromotion(type: PieceType) {
        val session = _state.value.session ?: return
        if (!_state.value.canInput || _state.value.moves.none { it.from == session.source && it.to == session.destination && it.promotion == type }) return
        render(session.copy(promotion = type))
        _state.update { it.copy(chooser = Chooser.NONE) }
        persist()
    }
    fun hint() {
        val session = _state.value.session ?: return
        if (!_state.value.canInput || session.hintUsed) return
        render(session.copy(hintUsed = true))
        _state.update { it.copy(hintVisible = true) }
        persist()
    }
    fun commitMove() {
        val state = _state.value
        if (!state.canCommit) return
        val session = state.session ?: return
        val puzzle = state.puzzle ?: return
        val source = session.source ?: return
        val destination = session.destination ?: return
        val move = Move(source, destination, session.promotion)
        val updated = evaluator.evaluate(puzzle, session, move) ?: return
        _state.update { it.copy(busy = true, chooser = Chooser.NONE) }
        render(updated)
        _state.update { it.copy(status = if (updated.outcome == Outcome.NEEDS_PRACTICE) GameStatus.MISTAKE else GameStatus.CORRECT) }
        if (updated.outcome != Outcome.NEEDS_PRACTICE && state.preferences.sound) sound.play()
        if (updated.phase == Phase.COMPLETED) progress.stageResult(updated)
        persist()
        job?.cancel()
        job = viewModelScope.launch {
            delay(if (state.preferences.reducedMotion) 120L else 200L)
            sound.release()
            if (updated.phase == Phase.COMPLETED) {
                delay(250L)
                showResult()
            } else {
                _state.update { it.copy(status = GameStatus.OPPONENT) }
                opponent()
            }
        }
    }
    private suspend fun opponent() {
        delay(GameConfig.REPLY_DELAY_MS)
        val session = _state.value.session ?: return
        val puzzle = _state.value.puzzle ?: return
        if (session.phase != Phase.OPPONENT) return
        val reply = puzzle.line.getOrNull(session.plies.size) ?: return
        val updated = session.copy(plies = session.plies + reply, phase = Phase.PLAYER)
        render(updated)
        _state.update { it.copy(busy = false, status = GameStatus.SELECT) }
        persist()
    }
    fun endAttempt() {
        val session = _state.value.session ?: return
        if (session.phase == Phase.COMPLETED || _state.value.busy) return
        job?.cancel()
        val updated = session.copy(phase = Phase.COMPLETED, outcome = Outcome.REVEALED, source = null, destination = null)
        render(updated)
        progress.stageResult(updated)
        persist()
        viewModelScope.launch { showResult() }
    }
    private suspend fun showResult() {
        _state.update { it.copy(busy = false, status = GameStatus.COMPLETE) }
        if (!routed) { routed = true; _events.send(id) }
    }
    fun viewResult() { viewModelScope.launch { _events.send(id) } }
    fun retrySave() { persist() }
    fun takeBreak() { pause(); _state.update { it.copy(pauseDialog = true) } }
    fun closeBreak() { _state.update { it.copy(pauseDialog = false) }; resume() }
    fun pause() {
        job?.cancel()
        job = null
        sound.release()
        _state.update { it.copy(paused = true, busy = false) }
    }
    fun resume() {
        if (_state.value.pauseDialog) return
        _state.update { it.copy(paused = false) }
        if (job?.isActive == true) return
        when (_state.value.session?.phase) {
            Phase.OPPONENT -> {
                _state.update { it.copy(busy = true, status = GameStatus.OPPONENT) }
                job = viewModelScope.launch { opponent() }
            }
            Phase.COMPLETED -> if (!routed) job = viewModelScope.launch { showResult() }
            else -> Unit
        }
    }
    override fun onCleared() { sound.release() }
}

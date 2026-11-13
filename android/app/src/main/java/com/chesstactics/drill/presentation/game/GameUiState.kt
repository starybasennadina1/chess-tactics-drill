package com.chesstactics.drill.presentation.game

import com.chesstactics.drill.domain.model.*

enum class Chooser { NONE, SOURCE, DESTINATION, PROMOTION }
enum class GameStatus { SELECT, DESTINATION, READY, OPPONENT, CORRECT, MISTAKE, COMPLETE, ERROR, PAUSED }
data class GameUiState(val puzzle: Puzzle? = null, val session: Session? = null, val board: Board? = null, val orientation: PieceColor = PieceColor.WHITE, val moves: List<Move> = emptyList(), val chooser: Chooser = Chooser.NONE, val hintVisible: Boolean = false, val hintSquare: Int? = null, val status: GameStatus = GameStatus.SELECT, val busy: Boolean = false, val paused: Boolean = false, val pauseDialog: Boolean = false, val storageError: Boolean = false, val preferences: Preferences = Preferences()) {
    val canInput: Boolean get() = session?.phase == Phase.PLAYER && !busy && !paused && !hintVisible
    val canCommit: Boolean get() = canInput && chooser == Chooser.NONE && moves.any { it.from == session?.source && it.to == session?.destination && it.promotion == session?.promotion }
}

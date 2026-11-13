package com.chesstactics.drill.domain.usecase

import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.domain.rules.ChessRules

class EvaluateMoveUseCase {
    fun evaluate(puzzle: Puzzle, session: Session, move: Move): Session? {
        if (session.phase != Phase.PLAYER) return null
        val board = ChessRules.replay(puzzle, session.plies)
        if (move !in ChessRules.legalMoves(board)) return null
        val correct = puzzle.line.getOrNull(session.plies.size) == move.uci
        val plies = session.plies + move.uci
        val terminal = correct && plies.size == puzzle.line.size
        val finalBoard = ChessRules.apply(board, move)
        if (terminal && puzzle.theme == PuzzleTheme.MATES_IN_TWO && !ChessRules.checkmate(finalBoard)) return null
        if (terminal && puzzle.capture != null && board.squares[move.to]?.type != puzzle.capture) return null
        val outcome = when {
            !correct -> Outcome.NEEDS_PRACTICE
            terminal -> if (session.hintUsed) Outcome.ASSISTED else Outcome.SOLVED
            else -> null
        }
        return session.copy(plies = plies, phase = if (outcome != null) Phase.COMPLETED else Phase.OPPONENT, source = null, destination = null, promotion = null, correctMoves = session.correctMoves + if (correct) 1 else 0, outcome = outcome)
    }
}

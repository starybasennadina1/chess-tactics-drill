package com.chesstactics.drill.domain.rules

import com.chesstactics.drill.domain.model.*

object PuzzleValidator {
    fun valid(puzzle: Puzzle): Boolean = runCatching {
        var board = ChessRules.parseFen(puzzle.fen)
        require(PieceColor.entries.all { color -> board.squares.count { it == Piece(PieceType.KING, color) } == 1 })
        require(!ChessRules.inCheck(board, board.turn.opposite()))
        require(puzzle.line.isNotEmpty() && puzzle.line.size % 2 == 1 && puzzle.san.size == puzzle.line.size)
        var captured: Piece? = null
        puzzle.line.forEach { uci ->
            val move = Move.parse(uci)
            captured = board.squares[move.to]
            board = ChessRules.apply(board, move)
        }
        if (puzzle.theme == PuzzleTheme.MATES_IN_TWO) require(ChessRules.checkmate(board)) else require(captured?.type == puzzle.capture)
    }.isSuccess
}

package com.chesstactics.drill.domain.rules

import com.chesstactics.drill.domain.model.*
import kotlin.math.abs

object ChessRules {
    fun parseFen(fen: String): Board {
        val fields = fen.split(' ')
        require(fields.size == 6)
        val rows = fields[0].split('/')
        require(rows.size == 8)
        val cells = MutableList<Piece?>(64) { null }
        rows.forEachIndexed { row, text ->
            var file = 0
            text.forEach { ch ->
                if (ch in '1'..'8') file += ch.digitToInt() else {
                    require(file in 0..7)
                    val type = when (ch.lowercaseChar()) {
                        'k' -> PieceType.KING
                        'q' -> PieceType.QUEEN
                        'r' -> PieceType.ROOK
                        'b' -> PieceType.BISHOP
                        'n' -> PieceType.KNIGHT
                        'p' -> PieceType.PAWN
                        else -> error("piece")
                    }
                    cells[(7 - row) * 8 + file] = Piece(type, if (ch.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK)
                    file++
                }
            }
            require(file == 8)
        }
        require(fields[1] == "w" || fields[1] == "b")
        val rights = fields[2].takeUnless { it == "-" }.orEmpty()
        require(rights.all { it in "KQkq" } && rights.toSet().size == rights.length)
        return Board(cells, if (fields[1] == "w") PieceColor.WHITE else PieceColor.BLACK, rights, fields[3].takeUnless { it == "-" }?.let(::square), fields[4].toInt(), fields[5].toInt())
    }

    fun inCheck(board: Board, color: PieceColor): Boolean {
        val king = board.squares.indexOf(Piece(PieceType.KING, color))
        return king < 0 || attacked(board, king, color.opposite())
    }

    fun attacked(board: Board, target: Int, by: PieceColor): Boolean = board.squares.indices.any { from ->
        val piece = board.squares[from]
        piece?.color == by && attacks(board, from, target, piece)
    }

    private fun attacks(board: Board, from: Int, to: Int, piece: Piece): Boolean {
        val dx = to % 8 - from % 8
        val dy = to / 8 - from / 8
        if (dx == 0 && dy == 0) return false
        return when (piece.type) {
            PieceType.PAWN -> abs(dx) == 1 && dy == (if (piece.color == PieceColor.WHITE) 1 else -1)
            PieceType.KNIGHT -> abs(dx) * abs(dy) == 2
            PieceType.KING -> abs(dx) <= 1 && abs(dy) <= 1
            PieceType.ROOK -> (dx == 0 || dy == 0) && clear(board, from, to)
            PieceType.BISHOP -> abs(dx) == abs(dy) && clear(board, from, to)
            PieceType.QUEEN -> (dx == 0 || dy == 0 || abs(dx) == abs(dy)) && clear(board, from, to)
        }
    }

    private fun clear(board: Board, from: Int, to: Int): Boolean {
        val sx = (to % 8 - from % 8).compareTo(0)
        val sy = (to / 8 - from / 8).compareTo(0)
        var x = from % 8 + sx
        var y = from / 8 + sy
        while (x != to % 8 || y != to / 8) {
            if (board.squares[y * 8 + x] != null) return false
            x += sx
            y += sy
        }
        return true
    }

    fun legalMoves(board: Board): List<Move> = buildList {
        board.squares.forEachIndexed { from, piece ->
            if (piece?.color == board.turn) {
                for (to in 0..63) {
                    val promotions = if (piece.type == PieceType.PAWN && to / 8 in listOf(0, 7)) listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT) else listOf(null)
                    promotions.forEach { promotion ->
                        val move = Move(from, to, promotion)
                        if (pseudoLegal(board, move, piece) && !inCheck(applyUnchecked(board, move), piece.color)) add(move)
                    }
                }
            }
        }
    }

    private fun pseudoLegal(board: Board, move: Move, piece: Piece): Boolean {
        val (from, to) = move
        if (from == to) return false
        val target = board.squares[to]
        if (target?.color == piece.color || target?.type == PieceType.KING) return false
        val dx = to % 8 - from % 8
        val dy = to / 8 - from / 8
        if (piece.type == PieceType.KING && abs(dx) == 2 && dy == 0) return castleAllowed(board, move, piece.color)
        if (piece.type != PieceType.PAWN) return attacks(board, from, to, piece)
        val direction = if (piece.color == PieceColor.WHITE) 1 else -1
        val start = if (direction == 1) 1 else 6
        val last = if (direction == 1) 7 else 0
        if ((to / 8 == last) != (move.promotion != null)) return false
        return if (dx == 0) {
            target == null && (dy == direction || (dy == 2 * direction && from / 8 == start && board.squares[from + direction * 8] == null))
        } else abs(dx) == 1 && dy == direction && (target != null || (board.enPassant == to && board.squares[to - direction * 8] == Piece(PieceType.PAWN, piece.color.opposite())))
    }

    private fun castleAllowed(board: Board, move: Move, color: PieceColor): Boolean {
        val rank = if (color == PieceColor.WHITE) 0 else 7
        if (move.from != rank * 8 + 4 || move.to !in listOf(rank * 8 + 2, rank * 8 + 6)) return false
        val kingSide = move.to % 8 == 6
        val right = if (color == PieceColor.WHITE) { if (kingSide) 'K' else 'Q' } else { if (kingSide) 'k' else 'q' }
        val rookSquare = rank * 8 + if (kingSide) 7 else 0
        if (right !in board.castling || board.squares[rookSquare] != Piece(PieceType.ROOK, color) || inCheck(board, color)) return false
        val emptyFiles = if (kingSide) 5..6 else 1..3
        if (emptyFiles.any { board.squares[rank * 8 + it] != null }) return false
        val pass = move.from + if (kingSide) 1 else -1
        return !inCheck(applyUnchecked(board, Move(move.from, pass)), color)
    }

    fun apply(board: Board, move: Move): Board {
        require(move in legalMoves(board))
        return applyUnchecked(board, move)
    }

    private fun applyUnchecked(board: Board, move: Move): Board {
        val piece = requireNotNull(board.squares[move.from])
        val cells = board.squares.toMutableList()
        val captured = cells[move.to]
        cells[move.from] = null
        cells[move.to] = piece.copy(type = move.promotion ?: piece.type)
        if (piece.type == PieceType.PAWN && move.to == board.enPassant && captured == null && move.from % 8 != move.to % 8) cells[move.to + if (piece.color == PieceColor.WHITE) -8 else 8] = null
        if (piece.type == PieceType.KING && abs(move.to - move.from) == 2) {
            val rookFrom = move.from / 8 * 8 + if (move.to > move.from) 7 else 0
            val rookTo = move.from + if (move.to > move.from) 1 else -1
            cells[rookTo] = cells[rookFrom]
            cells[rookFrom] = null
        }
        var rights = board.castling
        if (piece.type == PieceType.KING) rights = rights.filterNot { if (piece.color == PieceColor.WHITE) it in "KQ" else it in "kq" }
        listOf(0 to 'Q', 7 to 'K', 56 to 'q', 63 to 'k').forEach { (index, right) ->
            if (move.from == index || move.to == index) rights = rights.replace(right.toString(), "")
        }
        return Board(cells.toList(), board.turn.opposite(), rights, if (piece.type == PieceType.PAWN && abs(move.to - move.from) == 16) (move.to + move.from) / 2 else null, if (piece.type == PieceType.PAWN || captured != null) 0 else board.halfMove + 1, board.fullMove + if (board.turn == PieceColor.BLACK) 1 else 0)
    }

    fun checkmate(board: Board) = inCheck(board, board.turn) && legalMoves(board).isEmpty()
    fun replay(puzzle: Puzzle, plies: List<String>): Board = plies.fold(parseFen(puzzle.fen)) { board, uci -> apply(board, Move.parse(uci)) }
}

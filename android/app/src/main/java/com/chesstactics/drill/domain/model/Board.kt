package com.chesstactics.drill.domain.model

enum class PieceColor { WHITE, BLACK;
    fun opposite() = if (this == WHITE) BLACK else WHITE
}
enum class PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }
data class Piece(val type: PieceType, val color: PieceColor)
data class Move(val from: Int, val to: Int, val promotion: PieceType? = null) {
    val uci: String get() = coordinate(from) + coordinate(to) + when (promotion) {
        PieceType.QUEEN -> "q"
        PieceType.ROOK -> "r"
        PieceType.BISHOP -> "b"
        PieceType.KNIGHT -> "n"
        else -> ""
    }
    companion object {
        fun parse(value: String): Move {
            require(value.length in 4..5)
            val promotion = if (value.length == 5) when (value[4]) {
                'q' -> PieceType.QUEEN
                'r' -> PieceType.ROOK
                'b' -> PieceType.BISHOP
                'n' -> PieceType.KNIGHT
                else -> error("promotion")
            } else null
            return Move(square(value.take(2)), square(value.substring(2, 4)), promotion)
        }
    }
}
fun square(value: String): Int {
    require(value.length == 2 && value[0] in 'a'..'h' && value[1] in '1'..'8')
    return (value[1] - '1') * 8 + (value[0] - 'a')
}
fun coordinate(index: Int): String = "${('a'.code + index % 8).toChar()}${index / 8 + 1}"
data class Board(val squares: List<Piece?>, val turn: PieceColor, val castling: String = "", val enPassant: Int? = null, val halfMove: Int = 0, val fullMove: Int = 1)

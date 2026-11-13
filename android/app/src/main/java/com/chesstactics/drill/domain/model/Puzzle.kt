package com.chesstactics.drill.domain.model

enum class PuzzleTheme { FORKS, PINS, MATES_IN_TWO }
enum class Difficulty { ALL, EASY, MEDIUM, HARD;
    fun accepts(rating: Int) = when (this) {
        ALL -> true
        EASY -> rating in 400..650
        MEDIUM -> rating in 651..999
        HARD -> rating in 1000..1200
    }
}
data class Puzzle(val id: String, val theme: PuzzleTheme, val rating: Int, val fen: String, val line: List<String>, val san: List<String>, val capture: PieceType?) {
    val playerMoves: Int get() = (line.size + 1) / 2
}
enum class Outcome { SOLVED, ASSISTED, NEEDS_PRACTICE, REVEALED }
enum class Phase { PLAYER, OPPONENT, COMPLETED }
data class Session(val id: String, val puzzleId: String, val plies: List<String> = emptyList(), val phase: Phase = Phase.PLAYER, val source: Int? = null, val destination: Int? = null, val promotion: PieceType? = null, val hintUsed: Boolean = false, val correctMoves: Int = 0, val outcome: Outcome? = null)
data class AttemptResult(val session: Session, val timestamp: Long, val ratingBefore: Int, val ratingAfter: Int, val rated: Boolean) {
    val delta: Int get() = ratingAfter - ratingBefore
}

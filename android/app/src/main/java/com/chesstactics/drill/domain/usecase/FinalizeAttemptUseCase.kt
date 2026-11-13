package com.chesstactics.drill.domain.usecase

import com.chesstactics.drill.domain.model.*
import kotlin.math.pow
import kotlin.math.roundToInt

class FinalizeAttemptUseCase {
    fun finalize(progress: Progress, puzzle: Puzzle, session: Session, timestamp: Long): Pair<Progress, AttemptResult> {
        progress.results.find { it.session.id == session.id }?.let { return progress to it }
        require(session.phase == Phase.COMPLETED && session.outcome != null)
        val previous = progress.puzzles[puzzle.id] ?: PuzzleProgress()
        val rated = !previous.rated
        val score = when (session.outcome) { Outcome.SOLVED -> 1.0; Outcome.ASSISTED -> 0.5; else -> 0.0 }
        val expected = 1.0 / (1.0 + 10.0.pow((puzzle.rating - progress.rating) / 400.0))
        val rating = if (rated) (progress.rating + (24 * (score - expected)).roundToInt()).coerceIn(100, 2500) else progress.rating
        val record = AttemptResult(session, timestamp, progress.rating, rating, rated)
        val streak = if (session.outcome == Outcome.SOLVED) progress.streak + 1 else 0
        val detail = previous.copy(rated = true, clean = previous.clean || session.outcome == Outcome.SOLVED, assisted = previous.assisted || session.outcome == Outcome.ASSISTED, lastAttempt = timestamp)
        val history = progress.results + record
        val ratedHistory = history.filter { it.rated }
        val practice = history.filterNot { it.rated }.takeLast(100)
        return progress.copy(rating = rating, streak = streak, bestStreak = maxOf(streak, progress.bestStreak), attemptCount = progress.attemptCount + 1, puzzles = progress.puzzles + (puzzle.id to detail), results = (ratedHistory + practice).sortedBy { it.timestamp }, session = if (progress.session?.id == session.id) null else progress.session) to record
    }
}

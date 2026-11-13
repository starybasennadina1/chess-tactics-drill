package com.chesstactics.drill.domain.usecase

import com.chesstactics.drill.domain.model.*
import kotlin.math.abs

class SelectPuzzleUseCase {
    fun eligible(puzzles: List<Puzzle>, progress: Progress, filters: Filters): List<Puzzle> = puzzles.filter { puzzle ->
        puzzle.theme == filters.theme && filters.difficulty.accepts(puzzle.rating) && (!filters.unsolvedOnly || progress.puzzles[puzzle.id]?.solved != true)
    }.sortedWith(compareBy<Puzzle> { progress.puzzles[it.id]?.solved == true }.thenBy { abs(it.rating - progress.rating) }.thenBy { progress.puzzles[it.id]?.lastAttempt ?: 0L }.thenBy { it.id })
}

package com.chesstactics.drill.domain.repository

import com.chesstactics.drill.domain.model.Puzzle

interface PuzzleRepository {
    val puzzles: List<Puzzle>
    val invalidCount: Int
    fun find(id: String): Puzzle? = puzzles.find { it.id == id }
}

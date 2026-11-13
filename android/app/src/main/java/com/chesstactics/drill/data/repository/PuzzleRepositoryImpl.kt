package com.chesstactics.drill.data.repository

import com.chesstactics.drill.data.sample.SampleData
import com.chesstactics.drill.domain.repository.PuzzleRepository
import com.chesstactics.drill.domain.rules.PuzzleValidator

class PuzzleRepositoryImpl : PuzzleRepository {
    override val puzzles = SampleData.puzzles.filter(PuzzleValidator::valid)
    override val invalidCount = SampleData.puzzles.size - puzzles.size
}

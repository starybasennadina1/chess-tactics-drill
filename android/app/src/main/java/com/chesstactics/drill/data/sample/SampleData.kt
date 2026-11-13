package com.chesstactics.drill.data.sample

import com.chesstactics.drill.domain.model.*

object SampleData {
    val puzzles = listOf(
        Puzzle("fork_01", PuzzleTheme.FORKS, 450, "3q3k/8/8/4N3/8/8/8/6K1 w - - 0 1", listOf("e5f7", "h8g8", "f7d8"), listOf("Nf7+", "Kg8", "Nxd8"), PieceType.QUEEN),
        Puzzle("fork_02", PuzzleTheme.FORKS, 650, "k3q3/8/8/3N4/8/8/8/1K6 w - - 0 1", listOf("d5c7", "a8b8", "c7e8"), listOf("Nc7+", "Kb8", "Nxe8"), PieceType.QUEEN),
        Puzzle("fork_03", PuzzleTheme.FORKS, 850, "6k1/3r4/8/8/4N3/8/8/6K1 w - - 0 1", listOf("e4f6", "g8g7", "f6d7"), listOf("Nf6+", "Kg7", "Nxd7"), PieceType.ROOK),
        Puzzle("fork_04", PuzzleTheme.FORKS, 1050, "6k1/8/8/8/4n3/8/8/3Q3K b - - 0 1", listOf("e4f2", "h1g1", "f2d1"), listOf("Nf2+", "Kg1", "Nxd1"), PieceType.QUEEN),
        Puzzle("pin_01", PuzzleTheme.PINS, 500, "4k3/4n3/8/3q4/2B5/8/8/4R1K1 w - - 0 1", listOf("c4d5"), listOf("Bxd5"), PieceType.QUEEN),
        Puzzle("pin_02", PuzzleTheme.PINS, 700, "3k4/3n4/8/4q3/5B2/8/8/1K1R4 w - - 0 1", listOf("f4e5"), listOf("Bxe5"), PieceType.QUEEN),
        Puzzle("pin_03", PuzzleTheme.PINS, 900, "4k3/8/2n5/1B2q3/8/8/4Q3/6K1 w - - 0 1", listOf("e2e5"), listOf("Qxe5+"), PieceType.QUEEN),
        Puzzle("pin_04", PuzzleTheme.PINS, 1100, "6k1/4q3/8/8/1b2Q3/2N5/8/4K3 b - - 0 1", listOf("e7e4"), listOf("Qxe4+"), PieceType.QUEEN),
        Puzzle("mate_01", PuzzleTheme.MATES_IN_TWO, 600, "5rk1/5ppp/3Q4/6B1/8/8/8/2R3K1 w - - 0 1", listOf("d6f8", "g8f8", "c1c8"), listOf("Qxf8+", "Kxf8", "Rc8#"), null),
        Puzzle("mate_02", PuzzleTheme.MATES_IN_TWO, 800, "1kr5/ppp5/4Q3/1B6/8/8/8/1K3R2 w - - 0 1", listOf("e6c8", "b8c8", "f1f8"), listOf("Qxc8+", "Kxc8", "Rf8#"), null),
        Puzzle("mate_03", PuzzleTheme.MATES_IN_TWO, 1000, "2r3k1/8/8/8/6b1/3q4/5PPP/5RK1 b - - 0 1", listOf("d3f1", "g1f1", "c8c1"), listOf("Qxf1+", "Kxf1", "Rc1#"), null),
        Puzzle("mate_04", PuzzleTheme.MATES_IN_TWO, 1200, "1k3r2/8/8/8/1b6/4q3/PPP5/1KR5 b - - 0 1", listOf("e3c1", "b1c1", "f8f1"), listOf("Qxc1+", "Kxc1", "Rf1#"), null)
    )
}

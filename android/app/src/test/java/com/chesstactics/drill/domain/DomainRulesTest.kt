package com.chesstactics.drill.domain

import com.chesstactics.drill.data.sample.SampleData
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.domain.rules.*
import com.chesstactics.drill.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test

class DomainRulesTest {
    @Test
    fun everyCuratedLineIsLegalAndAchievesItsGoal() {
        assertEquals(12, SampleData.puzzles.size)
        SampleData.puzzles.forEach { puzzle ->
            assertTrue(puzzle.id, PuzzleValidator.valid(puzzle))
            var session = Session(puzzle.id, puzzle.id)
            puzzle.line.forEachIndexed { index, uci ->
                if (index % 2 == 0) {
                    session = requireNotNull(EvaluateMoveUseCase().evaluate(puzzle, session, Move.parse(uci)))
                } else {
                    if (puzzle.theme == PuzzleTheme.MATES_IN_TWO) assertEquals(1, ChessRules.legalMoves(ChessRules.replay(puzzle, session.plies)).size)
                    session = session.copy(plies = session.plies + uci, phase = Phase.PLAYER)
                }
            }
            assertEquals(Outcome.SOLVED, session.outcome)
            assertEquals(puzzle.playerMoves, session.correctMoves)
        }
    }

    @Test
    fun moveGenerationMatchesReferencePositions() {
        val start = ChessRules.parseFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        assertEquals(20, perft(start, 1))
        assertEquals(400, perft(start, 2))
        assertEquals(8902, perft(start, 3))
        val castlePosition = ChessRules.parseFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1")
        assertEquals(48, perft(castlePosition, 1))
        assertEquals(2039, perft(castlePosition, 2))
    }

    @Test
    fun pinsAndEnPassantCannotExposeTheKing() {
        val pin = SampleData.puzzles.first { it.id == "pin_01" }
        val captured = ChessRules.replay(pin, pin.line)
        assertFalse(Move.parse("e7d5") in ChessRules.legalMoves(captured))
        val pinnedPawn = ChessRules.parseFen("k3r3/8/8/3pP3/8/8/8/4K3 w - d6 0 1")
        assertFalse(Move.parse("e5d6") in ChessRules.legalMoves(pinnedPawn))
        val freePawn = ChessRules.parseFen("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1")
        val result = ChessRules.apply(freePawn, Move.parse("e5d6"))
        assertNull(result.squares[square("d5")])
        assertEquals(Piece(PieceType.PAWN, PieceColor.WHITE), result.squares[square("d6")])
        assertNull(result.enPassant)
    }

    @Test
    fun castlingAndPromotionHaveCorrectEffects() {
        val board = ChessRules.parseFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        val castled = ChessRules.apply(board, Move.parse("e1g1"))
        assertEquals(Piece(PieceType.KING, PieceColor.WHITE), castled.squares[square("g1")])
        assertEquals(Piece(PieceType.ROOK, PieceColor.WHITE), castled.squares[square("f1")])
        assertEquals("kq", castled.castling)
        val attackedTransit = ChessRules.parseFen("k4r2/8/8/8/8/8/8/4K2R w K - 0 1")
        assertFalse(Move.parse("e1g1") in ChessRules.legalMoves(attackedTransit))
        val promotion = ChessRules.parseFen("4k3/P7/8/8/8/8/8/4K3 w - - 0 1")
        assertEquals(4, ChessRules.legalMoves(promotion).count { it.from == square("a7") && it.to == square("a8") })
        assertEquals(PieceType.KNIGHT, ChessRules.apply(promotion, Move.parse("a7a8n")).squares[square("a8")]?.type)
    }

    @Test
    fun wrongLegalMoveEndsAttemptAndIllegalMoveDoesNot() {
        SampleData.puzzles.forEach { puzzle ->
            val session = Session(puzzle.id, puzzle.id)
            val board = ChessRules.parseFen(puzzle.fen)
            val wrong = ChessRules.legalMoves(board).first { it.uci != puzzle.line.first() }
            val result = requireNotNull(EvaluateMoveUseCase().evaluate(puzzle, session, wrong))
            assertEquals(Phase.COMPLETED, result.phase)
            assertEquals(Outcome.NEEDS_PRACTICE, result.outcome)
            assertEquals(listOf(wrong.uci), result.plies)
            assertNull(EvaluateMoveUseCase().evaluate(puzzle, session, Move(0, 0)))
        }
    }

    @Test
    fun ratingUsesExactFormulaAndFinalizationIsIdempotent() {
        val puzzle = SampleData.puzzles.first { it.id == "mate_02" }
        val finalizer = FinalizeAttemptUseCase()
        val solved = Session("one", puzzle.id, puzzle.line, Phase.COMPLETED, correctMoves = 2, outcome = Outcome.SOLVED)
        val (progress, result) = finalizer.finalize(Progress(), puzzle, solved, 1L)
        assertEquals(812, result.ratingAfter)
        assertEquals(1, progress.streak)
        assertTrue(requireNotNull(progress.puzzles[puzzle.id]).clean)
        assertEquals(progress, finalizer.finalize(progress, puzzle, solved, 2L).first)
        assertEquals(800, finalizer.finalize(Progress(), puzzle, solved.copy(id = "hint", hintUsed = true, outcome = Outcome.ASSISTED), 1L).second.ratingAfter)
        assertEquals(788, finalizer.finalize(Progress(), puzzle, solved.copy(id = "wrong", outcome = Outcome.NEEDS_PRACTICE), 1L).second.ratingAfter)
        val repeated = finalizer.finalize(progress, puzzle, solved.copy(id = "repeat"), 3L)
        assertFalse(repeated.second.rated)
        assertEquals(0, repeated.second.delta)
        assertEquals(2, repeated.first.streak)
        assertEquals(1, repeated.first.puzzles.values.count { it.solved })
    }

    private fun perft(board: Board, depth: Int): Int {
        if (depth == 0) return 1
        return ChessRules.legalMoves(board).sumOf { perft(ChessRules.apply(board, it), depth - 1) }
    }

    @Test
    fun deterministicSelectionRespectsFiltersAndSolvedFlags() {
        val selector = SelectPuzzleUseCase()
        val library = SampleData.puzzles
        assertEquals("fork_03", selector.eligible(library, Progress(), Filters()).first().id)
        val progress = Progress(puzzles = mapOf("fork_03" to PuzzleProgress(clean = true)))
        assertEquals("fork_02", selector.eligible(library, progress, Filters()).first().id)
        assertEquals(listOf("fork_04"), selector.eligible(library, progress, Filters(difficulty = Difficulty.HARD)).map { it.id })
        val completed = Progress(puzzles = library.associate { it.id to PuzzleProgress(assisted = true) })
        assertTrue(selector.eligible(library, completed, Filters()).isEmpty())
        assertEquals(4, selector.eligible(library, completed, Filters(unsolvedOnly = false)).size)
    }

    @Test
    fun invalidPuzzleContentIsRejected() {
        val valid = SampleData.puzzles.first()
        assertFalse(PuzzleValidator.valid(valid.copy(fen = "8/8/8/8/8/8/8/8 w - - 0 1")))
        assertFalse(PuzzleValidator.valid(valid.copy(line = listOf("e5e8"), san = listOf("Ne8"))))
        assertFalse(PuzzleValidator.valid(valid.copy(capture = PieceType.ROOK)))
        assertFalse(PuzzleValidator.valid(valid.copy(theme = PuzzleTheme.MATES_IN_TWO, capture = null)))
    }
}

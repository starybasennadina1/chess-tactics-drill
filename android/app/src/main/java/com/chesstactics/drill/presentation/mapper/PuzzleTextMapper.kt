package com.chesstactics.drill.presentation.mapper

import com.chesstactics.drill.R
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.presentation.game.GameStatus

fun PuzzleTheme.titleRes() = when (this) { PuzzleTheme.FORKS -> R.string.forks; PuzzleTheme.PINS -> R.string.pins; PuzzleTheme.MATES_IN_TWO -> R.string.mates }
fun PuzzleTheme.descriptionRes() = when (this) { PuzzleTheme.FORKS -> R.string.forks_description; PuzzleTheme.PINS -> R.string.pins_description; PuzzleTheme.MATES_IN_TWO -> R.string.mates_description }
fun PuzzleTheme.goalRes() = when (this) { PuzzleTheme.FORKS -> R.string.fork_goal; PuzzleTheme.PINS -> R.string.pin_goal; PuzzleTheme.MATES_IN_TWO -> R.string.mate_goal }
fun Difficulty.labelRes() = when (this) { Difficulty.ALL -> R.string.all; Difficulty.EASY -> R.string.easy; Difficulty.MEDIUM -> R.string.medium; Difficulty.HARD -> R.string.hard }
fun PieceType.labelRes() = when (this) { PieceType.KING -> R.string.king; PieceType.QUEEN -> R.string.queen; PieceType.ROOK -> R.string.rook; PieceType.BISHOP -> R.string.bishop; PieceType.KNIGHT -> R.string.knight; PieceType.PAWN -> R.string.pawn }
fun PieceColor.labelRes() = if (this == PieceColor.WHITE) R.string.white else R.string.black
fun BoardStyle.labelRes() = when (this) { BoardStyle.CLASSIC -> R.string.classic; BoardStyle.LAVENDER -> R.string.lavender_style; BoardStyle.HIGH_CONTRAST -> R.string.high_contrast }
fun Outcome.labelRes() = when (this) { Outcome.SOLVED -> R.string.solved_title; Outcome.ASSISTED -> R.string.assisted_title; Outcome.NEEDS_PRACTICE -> R.string.mistake_title; Outcome.REVEALED -> R.string.revealed_title }
fun GameStatus.labelRes() = when (this) {
    GameStatus.SELECT -> R.string.select_piece_status
    GameStatus.DESTINATION -> R.string.select_square_status
    GameStatus.READY -> R.string.ready_status
    GameStatus.OPPONENT -> R.string.opponent_status
    GameStatus.CORRECT -> R.string.correct_status
    GameStatus.MISTAKE -> R.string.mistake_status
    GameStatus.COMPLETE -> R.string.complete_status
    GameStatus.ERROR -> R.string.content_error
    GameStatus.PAUSED -> R.string.paused_status
}
fun Puzzle.titleRes() = when (id) {
    "fork_01" -> R.string.fork_01_title; "fork_02" -> R.string.fork_02_title; "fork_03" -> R.string.fork_03_title; "fork_04" -> R.string.fork_04_title
    "pin_01" -> R.string.pin_01_title; "pin_02" -> R.string.pin_02_title; "pin_03" -> R.string.pin_03_title; "pin_04" -> R.string.pin_04_title
    "mate_01" -> R.string.mate_01_title; "mate_02" -> R.string.mate_02_title; "mate_03" -> R.string.mate_03_title; else -> R.string.mate_04_title
}
fun Puzzle.explanationRes() = when (id) {
    "fork_01" -> R.string.fork_01_explanation; "fork_02" -> R.string.fork_02_explanation; "fork_03" -> R.string.fork_03_explanation; "fork_04" -> R.string.fork_04_explanation
    "pin_01" -> R.string.pin_01_explanation; "pin_02" -> R.string.pin_02_explanation; "pin_03" -> R.string.pin_03_explanation; "pin_04" -> R.string.pin_04_explanation
    "mate_01" -> R.string.mate_01_explanation; "mate_02" -> R.string.mate_02_explanation; "mate_03" -> R.string.mate_03_explanation; else -> R.string.mate_04_explanation
}

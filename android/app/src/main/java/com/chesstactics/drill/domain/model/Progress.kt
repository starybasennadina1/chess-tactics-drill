package com.chesstactics.drill.domain.model

enum class BoardStyle { CLASSIC, LAVENDER, HIGH_CONTRAST }
data class Preferences(val boardStyle: BoardStyle = BoardStyle.CLASSIC, val sound: Boolean = true, val reducedMotion: Boolean = false)
data class Filters(val theme: PuzzleTheme = PuzzleTheme.FORKS, val difficulty: Difficulty = Difficulty.ALL, val unsolvedOnly: Boolean = true)
data class PuzzleProgress(val rated: Boolean = false, val clean: Boolean = false, val assisted: Boolean = false, val lastAttempt: Long = 0L) {
    val solved: Boolean get() = clean || assisted
}
data class Progress(val rating: Int = 800, val streak: Int = 0, val bestStreak: Int = 0, val attemptCount: Int = 0, val puzzles: Map<String, PuzzleProgress> = emptyMap(), val results: List<AttemptResult> = emptyList(), val preferences: Preferences = Preferences(), val filters: Filters = Filters(), val session: Session? = null)
data class StoreState(val loading: Boolean = true, val progress: Progress = Progress(), val error: Boolean = false, val corruptSession: Boolean = false, val ready: Boolean = false)

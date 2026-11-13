package com.chesstactics.drill.presentation.menu

import com.chesstactics.drill.domain.model.*

data class MenuUiState(val loading: Boolean = true, val ready: Boolean = false, val busy: Boolean = false, val filters: Filters = Filters(), val rating: Int = 800, val bestStreak: Int = 0, val puzzles: List<Puzzle> = emptyList(), val solved: Set<String> = emptySet(), val eligible: List<Puzzle> = emptyList(), val resume: Session? = null, val error: Boolean = false, val contentError: Boolean = false, val corruptSession: Boolean = false, val pendingCount: Int = 0)

package com.chesstactics.drill.domain.repository

import com.chesstactics.drill.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface ProgressRepository {
    val state: StateFlow<StoreState>
    val pending: StateFlow<List<Session>>
    suspend fun load()
    suspend fun setFilters(filters: Filters): Boolean
    suspend fun setPreferences(preferences: Preferences): Boolean
    suspend fun createSession(puzzleId: String): Session?
    suspend fun saveSession(session: Session): Boolean
    fun session(id: String): Session?
    fun stageResult(session: Session)
    suspend fun finalize(session: Session): AttemptResult?
    suspend fun reset(): Boolean
}

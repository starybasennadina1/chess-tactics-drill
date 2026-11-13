package com.chesstactics.drill.data.repository

import com.chesstactics.drill.data.local.PreferencesStore
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.domain.repository.*
import com.chesstactics.drill.domain.rules.ChessRules
import com.chesstactics.drill.domain.usecase.FinalizeAttemptUseCase
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ProgressRepositoryImpl(private val store: PreferencesStore, private val library: PuzzleRepository) : ProgressRepository {
    private val _state = MutableStateFlow(StoreState())
    override val state = _state.asStateFlow()
    private val _pending = MutableStateFlow<List<Session>>(emptyList())
    override val pending = _pending.asStateFlow()
    private val sessions = ConcurrentHashMap<String, Session>()
    private val mutex = Mutex()
    private val finalizer = FinalizeAttemptUseCase()

    override suspend fun load() = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (_state.value.ready) return@withLock
            _state.update { it.copy(loading = true, error = false) }
            runCatching { store.read() }.onSuccess { (loaded, corrupt) ->
                var damaged = corrupt
                val session = loaded.session?.takeIf { item ->
                    val valid = validSession(item)
                    if (!valid) damaged = true
                    valid && loaded.results.none { it.session.id == item.id }
                }
                val progress = loaded.copy(session = session)
                progress.results.forEach { sessions[it.session.id] = it.session }
                session?.let { sessions[it.id] = it; if (it.phase == Phase.COMPLETED) stageResult(it) }
                _state.value = StoreState(false, progress, false, damaged, true)
            }.onFailure { _state.update { it.copy(loading = false, error = true) } }
        }
    }

    private fun validSession(session: Session): Boolean = runCatching {
        val puzzle = requireNotNull(library.find(session.puzzleId))
        require(session.plies.size <= puzzle.line.size && session.id.isNotEmpty())
        val board = ChessRules.replay(puzzle, session.plies)
        if (session.phase != Phase.COMPLETED) {
            require(session.plies == puzzle.line.take(session.plies.size))
            require(session.plies.size < puzzle.line.size)
            require((session.phase == Phase.OPPONENT) == (session.plies.size % 2 == 1))
            require(session.outcome == null)
            val legal = ChessRules.legalMoves(board)
            session.source?.let { source -> require(legal.any { it.from == source }) }
            session.destination?.let { destination -> require(legal.any { it.from == session.source && it.to == destination }) }
        } else require(session.outcome != null)
    }.isSuccess

    private fun commit(progress: Progress): Boolean {
        val success = runCatching { store.write(progress) }.getOrDefault(false)
        if (success) _state.update { it.copy(progress = progress, error = false) } else _state.update { it.copy(error = true) }
        return success
    }

    override suspend fun setFilters(filters: Filters): Boolean = withContext(Dispatchers.IO) { mutex.withLock { _state.value.ready && commit(_state.value.progress.copy(filters = filters)) } }
    override suspend fun setPreferences(preferences: Preferences): Boolean = withContext(Dispatchers.IO) { mutex.withLock { _state.value.ready && commit(_state.value.progress.copy(preferences = preferences)) } }

    override suspend fun createSession(puzzleId: String): Session? = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!_state.value.ready || library.find(puzzleId) == null || _pending.value.isNotEmpty()) return@withLock null
            val session = Session(UUID.randomUUID().toString(), puzzleId)
            if (commit(_state.value.progress.copy(session = session))) { sessions[session.id] = session; session } else null
        }
    }

    override suspend fun saveSession(session: Session): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!_state.value.ready) return@withLock false
            if (_state.value.progress.results.any { it.session.id == session.id }) return@withLock true
            if (sessions[session.id]?.phase == Phase.COMPLETED && session.phase != Phase.COMPLETED) return@withLock true
            sessions[session.id] = session
            val active = _state.value.progress.session
            if (active != null && active.id != session.id) return@withLock true
            commit(_state.value.progress.copy(session = session))
        }
    }

    override fun session(id: String): Session? = sessions[id]

    override fun stageResult(session: Session) {
        sessions[session.id] = session
        if (_state.value.progress.results.none { it.session.id == session.id }) _pending.update { items -> items.filterNot { it.id == session.id } + session }
    }

    override suspend fun finalize(session: Session): AttemptResult? = withContext(Dispatchers.IO) {
        mutex.withLock {
            val progress = _state.value.progress
            progress.results.find { it.session.id == session.id }?.let { existing ->
                _pending.update { items -> items.filterNot { it.id == session.id } }
                return@withLock existing
            }
            val puzzle = library.find(session.puzzleId) ?: return@withLock null
            if (!_state.value.ready) return@withLock null
            val (updated, result) = finalizer.finalize(progress, puzzle, session, System.currentTimeMillis())
            if (commit(updated)) {
                _pending.update { items -> items.filterNot { it.id == session.id } }
                result
            } else null
        }
    }

    override suspend fun reset(): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!_state.value.ready) return@withLock false
            if (commit(Progress(preferences = _state.value.progress.preferences))) {
                sessions.clear()
                _pending.value = emptyList()
                true
            } else false
        }
    }
}

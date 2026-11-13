package com.chesstactics.drill.data.local

import android.content.Context
import com.chesstactics.drill.domain.model.*
import org.json.JSONArray
import org.json.JSONObject

class PreferencesStore(context: Context) {
    private val preferences = context.getSharedPreferences("chess_tactics_drill_prefs", Context.MODE_PRIVATE)

    fun read(): Pair<Progress, Boolean> {
        val raw = preferences.getString("snapshot", null) ?: return Progress() to false
        val json = JSONObject(raw)
        require(json.getInt("version") == 1)
        val settings = json.getJSONObject("settings")
        val filters = json.getJSONObject("filters")
        val details = json.getJSONObject("puzzles")
        val puzzles = details.keys().asSequence().associateWith { key ->
            val item = details.getJSONObject(key)
            PuzzleProgress(item.getBoolean("rated"), item.getBoolean("clean"), item.getBoolean("assisted"), item.getLong("last"))
        }
        val history = json.getJSONArray("results")
        val records = (0 until history.length()).map { index ->
            val item = history.getJSONObject(index)
            AttemptResult(readSession(item.getJSONObject("session")), item.getLong("time"), item.getInt("before"), item.getInt("after"), item.getBoolean("rated"))
        }
        var corrupt = false
        val active = if (json.isNull("session")) null else runCatching { readSession(json.getJSONObject("session")) }.getOrElse { corrupt = true; null }
        val rating = json.getInt("rating")
        require(rating in 100..2500 && records.map { it.session.id }.distinct().size == records.size)
        return Progress(rating, json.getInt("streak"), json.getInt("best"), json.getInt("attempts"), puzzles, records,
            Preferences(BoardStyle.valueOf(settings.getString("style")), settings.getBoolean("sound"), settings.getBoolean("motion")),
            Filters(PuzzleTheme.valueOf(filters.getString("theme")), Difficulty.valueOf(filters.getString("difficulty")), filters.getBoolean("unsolved")), active) to corrupt
    }

    fun write(progress: Progress): Boolean {
        val details = JSONObject()
        progress.puzzles.forEach { (id, item) -> details.put(id, JSONObject().put("rated", item.rated).put("clean", item.clean).put("assisted", item.assisted).put("last", item.lastAttempt)) }
        val history = JSONArray()
        progress.results.forEach { item -> history.put(JSONObject().put("session", sessionJson(item.session)).put("time", item.timestamp).put("before", item.ratingBefore).put("after", item.ratingAfter).put("rated", item.rated)) }
        val settings = progress.preferences
        val filters = progress.filters
        val json = JSONObject().put("version", 1).put("rating", progress.rating).put("streak", progress.streak).put("best", progress.bestStreak).put("attempts", progress.attemptCount)
            .put("settings", JSONObject().put("style", settings.boardStyle.name).put("sound", settings.sound).put("motion", settings.reducedMotion))
            .put("filters", JSONObject().put("theme", filters.theme.name).put("difficulty", filters.difficulty.name).put("unsolved", filters.unsolvedOnly))
            .put("puzzles", details).put("results", history).put("session", progress.session?.let(::sessionJson) ?: JSONObject.NULL)
        return preferences.edit().putString("snapshot", json.toString()).commit()
    }

    private fun sessionJson(session: Session): JSONObject = JSONObject().put("id", session.id).put("puzzle", session.puzzleId).put("fenVersion", 1)
        .put("plies", JSONArray(session.plies)).put("phase", session.phase.name).put("source", session.source ?: -1).put("destination", session.destination ?: -1)
        .put("promotion", session.promotion?.name ?: "").put("hint", session.hintUsed).put("correct", session.correctMoves).put("outcome", session.outcome?.name ?: "")

    private fun readSession(json: JSONObject): Session {
        require(json.getInt("fenVersion") == 1)
        val plies = json.getJSONArray("plies")
        return Session(json.getString("id"), json.getString("puzzle"), (0 until plies.length()).map { plies.getString(it) }, Phase.valueOf(json.getString("phase")),
            json.getInt("source").takeIf { it in 0..63 }, json.getInt("destination").takeIf { it in 0..63 }, json.getString("promotion").takeIf { it.isNotEmpty() }?.let(PieceType::valueOf),
            json.getBoolean("hint"), json.getInt("correct"), json.getString("outcome").takeIf { it.isNotEmpty() }?.let(Outcome::valueOf))
    }
}

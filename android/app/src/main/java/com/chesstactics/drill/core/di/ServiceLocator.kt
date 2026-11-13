package com.chesstactics.drill.core.di

import android.content.Context
import android.provider.Settings
import com.chesstactics.drill.core.audio.SoundFeedback
import com.chesstactics.drill.data.local.PreferencesStore
import com.chesstactics.drill.data.repository.*
import com.chesstactics.drill.domain.repository.*

object ServiceLocator {
    private var dependencies: Dependencies? = null
    val services: Dependencies get() = requireNotNull(dependencies)
    fun initialize(context: Context) {
        if (dependencies == null) dependencies = Dependencies(context.applicationContext)
    }
}
class Dependencies(private val context: Context) {
    val puzzles: PuzzleRepository = PuzzleRepositoryImpl()
    val progress: ProgressRepository = ProgressRepositoryImpl(PreferencesStore(context), puzzles)
    val systemReducedMotion: Boolean get() = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    fun sound() = SoundFeedback(context)
}

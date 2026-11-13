package com.chesstactics.drill.core.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator

class SoundFeedback(context: Context) {
    private val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var tone: ToneGenerator? = null
    fun play() {
        if (manager.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        release()
        tone = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 10).also { it.startTone(ToneGenerator.TONE_PROP_ACK, 60) } }.getOrNull()
    }
    fun release() { tone?.release(); tone = null }
}

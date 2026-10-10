package com.elfachtali.bruitblanc.audio

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class PlaybackState(
    val playing: Boolean = false,
    val type: NoiseType = NoiseType.WHITE,
    val volume: Float = 0.7f,
    /** Wall-clock time (ms) when playback stops, or null for no timer. */
    val stopAt: Long? = null,
)

/** Single source of truth shared by the UI and [NoiseService]. */
object Playback {
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state

    fun play(context: Context) {
        _state.update { it.copy(playing = true) }
        ContextCompat.startForegroundService(context, Intent(context, NoiseService::class.java))
    }

    fun stop(context: Context) {
        _state.update { it.copy(playing = false, stopAt = null) }
        context.stopService(Intent(context, NoiseService::class.java))
    }

    fun setType(type: NoiseType) = _state.update { it.copy(type = type) }

    fun setVolume(volume: Float) = _state.update { it.copy(volume = volume.coerceIn(0f, 1f)) }

    fun setTimer(minutes: Int?) = _state.update {
        it.copy(stopAt = minutes?.let { m -> System.currentTimeMillis() + m * 60_000L })
    }

    internal fun markStopped() = _state.update { it.copy(playing = false, stopAt = null) }
}

package com.elfachtali.bruitblanc.audio

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** État de lecture affiché par l'interface et appliqué par [NoiseService]. */
data class PlaybackState(
    val playing: Boolean = false,
    val type: NoiseType = NoiseType.WHITE,
    val volume: Float = 0.7f,
    /** Heure d'arrêt (ms depuis l'epoch) de la minuterie, ou null sans minuterie. */
    val stopAt: Long? = null,
)

/**
 * Source de vérité unique, partagée par l'interface et par [NoiseService].
 *
 * L'interface modifie l'état via ces fonctions ; le service lit l'état pour jouer le bon son
 * au bon volume et pour appliquer la minuterie.
 */
object Playback {
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state

    /** Lance la lecture en démarrant le service de premier plan. */
    fun play(context: Context) {
        _state.update { it.copy(playing = true) }
        ContextCompat.startForegroundService(context, Intent(context, NoiseService::class.java))
    }

    /** Arrête la lecture et annule la minuterie. */
    fun stop(context: Context) {
        _state.update { it.copy(playing = false, stopAt = null) }
        context.stopService(Intent(context, NoiseService::class.java))
    }

    /** Change le type de bruit, y compris pendant la lecture. */
    fun setType(type: NoiseType) = _state.update { it.copy(type = type) }

    /** Règle le volume, borné entre 0 et 1. */
    fun setVolume(volume: Float) = _state.update { it.copy(volume = volume.coerceIn(0f, 1f)) }

    /** Programme l'arrêt dans [minutes] minutes, ou supprime la minuterie si null. */
    fun setTimer(minutes: Int?) = _state.update {
        it.copy(stopAt = minutes?.let { m -> System.currentTimeMillis() + m * 60_000L })
    }

    /** Appelé par le service quand il s'arrête de lui-même (minuterie, perte du focus audio). */
    internal fun markStopped() = _state.update { it.copy(playing = false, stopAt = null) }
}

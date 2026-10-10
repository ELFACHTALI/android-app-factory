package com.elfachtali.bruitblanc.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread

/**
 * Joue le bruit généré en continu sur un [AudioTrack] (PCM float mono, 44,1 kHz), depuis un
 * thread dédié, jusqu'à l'appel de [stop].
 *
 * Le type et le volume sont relus à chaque buffer (environ 93 ms), donc un changement dans
 * l'interface s'entend presque immédiatement, sans redémarrer la lecture.
 *
 * @param currentType lit le type de bruit courant.
 * @param currentVolume lit le volume courant, entre 0 et 1.
 */
class NoisePlayer(
    private val currentType: () -> NoiseType,
    private val currentVolume: () -> Float,
) {
    @Volatile private var running = false
    private var worker: Thread? = null

    /** Démarre la lecture ; sans effet si elle est déjà en cours. */
    fun start() {
        if (running) return
        running = true
        worker = thread(name = "noise-player", priority = Thread.MAX_PRIORITY) { play() }
    }

    /** Demande l'arrêt, puis attend (1 s au plus) le fondu de sortie et la libération de l'AudioTrack. */
    fun stop() {
        running = false
        worker?.join(1_000)
        worker = null
    }

    private fun play() {
        val minBuffer = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(minBuffer, CHUNK * 4 * 4))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        val generator = NoiseGenerator()
        val buffer = FloatArray(CHUNK)
        var gain = 0f
        track.play()
        try {
            while (running) {
                generator.fill(currentType(), buffer)
                val target = currentVolume()
                for (i in buffer.indices) {
                    // Rampe douce du gain : évite les clics au démarrage et quand le volume change.
                    gain += (target - gain) * RAMP
                    buffer[i] *= gain
                }
                track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
            }
            // Fondu de sortie sur un dernier buffer avant l'arrêt, pour éviter un clic.
            generator.fill(currentType(), buffer)
            for (i in buffer.indices) buffer[i] *= gain * (1f - i.toFloat() / buffer.size)
            track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
        } finally {
            track.stop()
            track.release()
        }
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
        const val CHUNK = 4_096
        /** Part de l'écart de gain rattrapée à chaque échantillon (environ 0,1 s pour converger). */
        const val RAMP = 0.0002f
    }
}

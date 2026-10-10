package com.elfachtali.bruitblanc.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread

/** Streams generated noise to an [AudioTrack] on a background thread until [stop] is called. */
class NoisePlayer(
    private val currentType: () -> NoiseType,
    private val currentVolume: () -> Float,
) {
    @Volatile private var running = false
    private var worker: Thread? = null

    fun start() {
        if (running) return
        running = true
        worker = thread(name = "noise-player", priority = Thread.MAX_PRIORITY) { play() }
    }

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
                    // Smooth ramp avoids clicks on start and when the volume changes.
                    gain += (target - gain) * RAMP
                    buffer[i] *= gain
                }
                track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
            }
            // Fade out before stopping.
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
        const val RAMP = 0.0002f
    }
}

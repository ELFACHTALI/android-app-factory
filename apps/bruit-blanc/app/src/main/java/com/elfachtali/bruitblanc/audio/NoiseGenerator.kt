package com.elfachtali.bruitblanc.audio

import kotlin.random.Random

enum class NoiseType { WHITE, PINK, BROWN }

/**
 * Generates noise samples in [-1, 1] entirely in code, so the app ships no audio files.
 * Pink noise uses Paul Kellet's economy filter; brown noise is leaky-integrated white noise.
 */
class NoiseGenerator(private val random: Random = Random.Default) {
    private var b0 = 0.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var brown = 0.0

    fun fill(type: NoiseType, out: FloatArray) {
        for (i in out.indices) {
            val white = random.nextDouble(-1.0, 1.0)
            val sample = when (type) {
                NoiseType.WHITE -> white * 0.5
                NoiseType.PINK -> {
                    b0 = 0.99765 * b0 + white * 0.0990460
                    b1 = 0.96300 * b1 + white * 0.2965164
                    b2 = 0.57000 * b2 + white * 1.0526913
                    (b0 + b1 + b2 + white * 0.1848) * 0.25
                }
                NoiseType.BROWN -> {
                    brown = (brown + 0.02 * white) / 1.02
                    brown * 3.5
                }
            }
            out[i] = sample.coerceIn(-1.0, 1.0).toFloat()
        }
    }
}

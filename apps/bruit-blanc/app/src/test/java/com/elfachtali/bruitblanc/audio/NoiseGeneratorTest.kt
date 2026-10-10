package com.elfachtali.bruitblanc.audio

import kotlin.math.abs
import kotlin.random.Random
import org.junit.Assert.assertTrue
import org.junit.Test

/** Vérifie que chaque type de bruit reste dans [-1, 1] sans être trop faible. */
class NoiseGeneratorTest {
    @Test
    fun samplesStayInRangeAndAreNotSilent() {
        for (type in NoiseType.entries) {
            val generator = NoiseGenerator(Random(42))
            val buffer = FloatArray(44_100)
            repeat(10) { generator.fill(type, buffer) }
            assertTrue("$type out of range", buffer.all { it in -1f..1f })
            val meanLevel = buffer.map { abs(it) }.average()
            assertTrue("$type too quiet: $meanLevel", meanLevel > 0.02)
        }
    }
}

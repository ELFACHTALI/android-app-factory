package com.elfachtali.bruitblanc.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.elfachtali.bruitblanc.audio.NoiseType
import com.elfachtali.bruitblanc.audio.Playback
import com.elfachtali.bruitblanc.ui.theme.BruitBlancTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the Play Store phone screenshots (1233 x 2460 px, under the 2:1 limit) straight into
 * the Gradle Play Publisher listing folders. Run with `./gradlew recordRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class StoreScreenshotsTest {
    @get:Rule val compose = createComposeRule()

    @After fun reset() {
        Playback.setType(NoiseType.WHITE)
        Playback.setTimer(null)
    }

    @Test @Config(qualifiers = "fr-w411dp-h820dp-night-xxhdpi")
    fun frenchHome() = shot("fr-FR", 1) { }

    @Test @Config(qualifiers = "fr-w411dp-h820dp-night-xxhdpi")
    fun frenchTimer() = shot("fr-FR", 2) { Playback.setType(NoiseType.BROWN); Playback.setTimer(60) }

    @Test @Config(qualifiers = "en-w411dp-h820dp-night-xxhdpi")
    fun englishHome() = shot("en-US", 1) { }

    @Test @Config(qualifiers = "en-w411dp-h820dp-night-xxhdpi")
    fun englishTimer() = shot("en-US", 2) { Playback.setType(NoiseType.BROWN); Playback.setTimer(60) }

    private fun shot(locale: String, index: Int, setUp: () -> Unit) {
        setUp()
        compose.setContent { BruitBlancTheme { NoiseScreen() } }
        compose.onRoot().captureRoboImage(
            "src/main/play/listings/$locale/graphics/phone-screenshots/$index.png"
        )
    }
}

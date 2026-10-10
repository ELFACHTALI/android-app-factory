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
 * Génère les captures d'écran du Play Store (1233 x 2460 px, sous la limite de ratio 2:1)
 * directement dans les dossiers de fiche de Gradle Play Publisher (src/main/play/listings).
 *
 * Lancement : `./gradlew recordRoborazziDebug` (fait par la CI à chaque build).
 * Robolectric dessine l'interface Compose sans émulateur ; les qualificateurs fixent la
 * langue, la taille d'écran, le mode nuit et la densité (dans cet ordre, imposé par Android).
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

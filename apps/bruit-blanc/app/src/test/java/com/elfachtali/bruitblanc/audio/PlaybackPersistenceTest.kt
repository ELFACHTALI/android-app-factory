package com.elfachtali.bruitblanc.audio

import android.content.Context
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Vérifie que le type de bruit et le volume choisis sont enregistrés pour le prochain lancement. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlaybackPersistenceTest {
    // Playback est un objet partagé par tous les tests : on remet les valeurs par défaut pour
    // ne pas changer les captures du Play Store générées ensuite.
    @After fun reset() {
        Playback.setType(NoiseType.WHITE)
        Playback.setVolume(PlaybackState().volume)
    }

    @Test
    fun typeAndVolumeAreSaved() {
        val app = RuntimeEnvironment.getApplication()
        Playback.restore(app)

        Playback.setType(NoiseType.BROWN)
        Playback.setVolume(1.5f) // borné à 1

        val prefs = app.getSharedPreferences("playback", Context.MODE_PRIVATE)
        assertEquals("BROWN", prefs.getString("type", null))
        assertEquals(1f, prefs.getFloat("volume", -1f), 0f)
        assertEquals(NoiseType.BROWN, Playback.state.value.type)
    }
}

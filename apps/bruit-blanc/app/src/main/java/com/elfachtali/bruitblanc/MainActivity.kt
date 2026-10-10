package com.elfachtali.bruitblanc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.elfachtali.bruitblanc.audio.Playback
import com.elfachtali.bruitblanc.ui.NoiseScreen
import com.elfachtali.bruitblanc.ui.theme.BruitBlancTheme

/** Unique écran de l'app : recharge les derniers réglages et affiche [NoiseScreen] dans le thème de nuit. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Playback.restore(this)
        enableEdgeToEdge()
        setContent {
            BruitBlancTheme {
                NoiseScreen()
            }
        }
    }
}

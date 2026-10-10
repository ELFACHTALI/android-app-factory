package com.elfachtali.bruitblanc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.elfachtali.bruitblanc.ui.NoiseScreen
import com.elfachtali.bruitblanc.ui.theme.BruitBlancTheme

/** Unique écran de l'app : affiche [NoiseScreen] dans le thème de nuit. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BruitBlancTheme {
                NoiseScreen()
            }
        }
    }
}

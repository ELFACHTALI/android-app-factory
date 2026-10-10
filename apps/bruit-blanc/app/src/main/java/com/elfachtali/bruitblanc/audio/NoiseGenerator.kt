package com.elfachtali.bruitblanc.audio

import kotlin.random.Random

/** Les trois couleurs de bruit proposées dans l'app. */
enum class NoiseType {
    /** Énergie égale sur toutes les fréquences : souffle aigu, masque bien les voix. */
    WHITE,

    /** Énergie qui baisse de 3 dB par octave : plus doux, proche de la pluie. */
    PINK,

    /** Énergie qui baisse de 6 dB par octave : grondement grave, proche d'une cascade. */
    BROWN,
}

/**
 * Génère les échantillons de bruit dans [-1, 1] entièrement par le code : l'app ne contient
 * aucun fichier audio, donc aucun droit d'auteur ni boucle audible.
 *
 * - Bruit rose : filtre « economy » de Paul Kellet (trois filtres passe-bas en parallèle).
 * - Bruit brun : bruit blanc intégré avec une fuite, pour ne pas dériver hors de [-1, 1].
 *
 * Les états des filtres sont conservés d'un appel à l'autre pour que le son reste continu
 * entre deux buffers. Une instance n'est pas thread-safe : un seul thread l'utilise.
 *
 * @param random source aléatoire, injectable pour des tests reproductibles.
 */
class NoiseGenerator(private val random: Random = Random.Default) {
    private var b0 = 0.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var brown = 0.0

    /** Remplit [out] avec des échantillons de bruit [type], de niveau comparable entre les types. */
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

# Bruit blanc

Application Android de bruit blanc, rose et brun, sans publicité et hors ligne, avec minuterie de sommeil. Premier POC de l'usine d'apps automatisée.

- Kotlin + Jetpack Compose, minSdk 26, targetSdk 36
- Sons générés par le code (`audio/NoiseGenerator.kt`), aucun fichier audio
- Lecture écran éteint via un service de premier plan `mediaPlayback`

## Build

```
./gradlew testDebugUnitTest assembleDebug
```

Le workflow `.github/workflows/build.yml` construit et teste chaque push. Le bundle signé (`bundleRelease`) n'est produit que si les secrets `UPLOAD_KEYSTORE_BASE64`, `UPLOAD_KEYSTORE_PASSWORD`, `UPLOAD_KEY_ALIAS` et `UPLOAD_KEY_PASSWORD` sont configurés.

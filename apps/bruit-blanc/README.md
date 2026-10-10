# Bruit blanc

Application Android de bruit blanc, rose et brun, sans publicité et hors ligne, avec minuterie de sommeil. Premier POC de l'usine d'apps automatisée.

- Kotlin + Jetpack Compose, minSdk 26, targetSdk 36
- Sons générés par le code (`audio/NoiseGenerator.kt`), aucun fichier audio
- Lecture écran éteint via un service de premier plan `mediaPlayback`

## Build

```
./gradlew testDebugUnitTest assembleDebug
```

Le workflow `.github/workflows/bruit-blanc.yml` construit et teste chaque push. Sur `main`, il produit aussi le bundle signé (`bundleRelease`) : la clé d'upload est gardée dans Secret Manager (GCP) et lue via Workload Identity Federation, sans aucun secret dans GitHub. Mise en place unique : `scripts/setup-signing.sh`, à lancer dans Cloud Shell.

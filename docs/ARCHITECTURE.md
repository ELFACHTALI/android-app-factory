# Architecture

L'usine prend un nom et une idée d'app, puis des agents Claude écrivent l'app, la construisent et la publient sur Google Play. La proposition complète (outils, coûts, contraintes Play) se trouve dans le [document de proposition](https://claude.ai/code/artifact/24405242-2270-478d-bcb0-40b23c798493). Ce fichier décrit ce qui existe réellement dans le dépôt.

## État au 2026-10-10

| Brique | État |
| --- | --- |
| App POC `apps/bruit-blanc` | Faite : code, tests, captures, fiche Play FR/EN |
| CI GitHub Actions | Faite : tests, lint, APK debug, bundle signé, fiche publiée sur la branche `store-listing` |
| Clé d'upload | Faite : dans Secret Manager (projet `android-app-factory-511217`), lue via Workload Identity Federation |
| Création de l'app dans la Play Console | Faite à la main (Google ne permet pas de la créer par API) ; version 8 (1.0.0) déposée à la main en test interne |
| Publication automatique (API Play Developer) | Active : chaque push sur `main` publie en test interne (Gradle Play Publisher, compte de service `github-signing` invité dans la Play Console) |
| Usine (service Cloud Run + agents) | À faire |
| GitHub App de l'usine (création des dépôts) | À faire |

## Organisation du dépôt

```
android-app-factory/
├── apps/bruit-blanc/            App POC, sert aussi de modèle pour les futures apps
│   ├── app/src/main/java/...    Code Kotlin (voir « App Bruit blanc » ci-dessous)
│   ├── app/src/main/play/       Fiche Play Store au format Gradle Play Publisher
│   ├── app/src/test/            Tests unitaires et génération des captures
│   └── store-art/               Sources HTML de l'icône 512 px et de la bannière
├── scripts/setup-signing.sh     Mise en place unique de la clé d'upload (Cloud Shell)
├── docs/                        Cette documentation et le journal des erreurs
└── .github/workflows/           Un workflow par app
```

## App Bruit blanc

Kotlin, Jetpack Compose (Material 3), minSdk 26, targetSdk 36. Aucun fichier audio, aucune donnée collectée, aucune connexion réseau.

| Fichier | Rôle |
| --- | --- |
| `audio/NoiseGenerator.kt` | Génère les échantillons de bruit blanc, rose et brun |
| `audio/NoisePlayer.kt` | Joue le flux en continu sur un `AudioTrack`, avec rampe de volume et fondu de sortie |
| `audio/Playback.kt` | État partagé (type, volume, lecture, minuterie) entre l'interface et le service |
| `audio/NoiseService.kt` | Service de premier plan `mediaPlayback` : lecture écran éteint, notification, focus audio, minuterie |
| `ui/NoiseScreen.kt` | L'unique écran : choix du bruit, lecture, volume, minuterie |
| `ui/theme/Theme.kt` | Thème de nuit |

Le flux : l'écran appelle `Playback` → `Playback` met à jour l'état et démarre ou arrête `NoiseService` → le service lance `NoisePlayer`, qui relit le type et le volume dans l'état à chaque buffer d'environ 93 ms.

## Chaîne de build et de signature

Le workflow `.github/workflows/bruit-blanc.yml` se lance à chaque push ou PR qui touche l'app :

1. `recordRoborazziDebug` : tests unitaires et rendu des captures d'écran du Play Store par Robolectric, sans émulateur.
2. `lintDebug` et `assembleDebug` : analyse statique et APK de test (artefact `debug-apk`).
3. Publication de la fiche générée : artefact `store-listing` et branche `store-listing`.
4. Sur `main` seulement : authentification à GCP par Workload Identity Federation, lecture de la clé d'upload dans Secret Manager, puis `bundleRelease` signé (artefact `release-aab`). Le `versionCode` est le numéro de run GitHub Actions.
5. Si `PLAY_PUBLISH` vaut `true` : `publishReleaseBundle` et `publishReleaseListing` envoient le bundle, les textes, les visuels et les notes de version sur la piste de test interne, avec le même compte de service (`github-signing`), qui doit être invité dans la Play Console.

Sécurité : la clé ne quitte jamais GCP et ne passe que par le runner GitHub, le temps du build. Le compte de service `github-signing` ne peut lire que les deux secrets de la clé, et seule la branche `main` de ce dépôt peut l'utiliser. En cas de perte, Play App Signing permet de réinitialiser la clé d'upload, puisque c'est Google qui conserve la clé de signature finale.

## Contraintes Google Play pour ce compte

- Compte personnel créé après le 13/11/2023 : chaque nouvelle app passe par un test fermé de 14 jours avec au moins 12 testeurs avant la production.
- App payante (1 €) : un profil de paiement doit être lié à la Play Console. Les testeurs de licence installent l'app gratuitement.
- Le service de premier plan `mediaPlayback` doit être déclaré dans la Play Console (Contenu de l'app).

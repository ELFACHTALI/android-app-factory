# Journal des erreurs et résolutions

Chaque problème rencontré pendant la construction de l'usine et des apps, avec sa cause et la résolution retenue. Les entrées les plus récentes sont en haut. Une nouvelle erreur s'ajoute ici dans le même commit que sa correction.

Format : **Symptôme** (ce qu'on a vu), **Cause**, **Résolution**, **Référence** (commit ou fichier).

---

## 2026-10-10 · Artefacts GitHub Actions impossibles à télécharger depuis la session Claude

- **Symptôme** : `gh run download` échoue avec « Forbidden » sur le stockage des artefacts. Impossible de vérifier les captures d'écran générées par la CI.
- **Cause** : le proxy réseau de la session Claude bloque le stockage des artefacts GitHub. Les journaux de build restent lisibles via l'API GitHub.
- **Résolution** : la CI publie la fiche Play générée (textes, icône, bannière, captures) sur la branche `store-listing`. On peut la consulter dans le navigateur, et Claude peut la lire avec `git fetch`.
- **Référence** : `.github/workflows/bruit-blanc.yml`, étape « Publish listing to the store-listing branch ».

## 2026-10-10 · Tests de captures : `IllegalArgumentException at Qualifiers.java:28`

- **Symptôme** : les 4 tests de `StoreScreenshotsTest` échouent dès le démarrage dans la CI.
- **Cause** : Robolectric exige les qualificateurs de ressources Android dans l'ordre officiel. `night` (mode nuit) doit venir avant `xxhdpi` (densité). On avait écrit `w411dp-h820dp-xxhdpi-night`.
- **Résolution** : `fr-w411dp-h820dp-night-xxhdpi` (langue, largeur, hauteur, mode nuit, densité).
- **Référence** : `apps/bruit-blanc/app/src/test/.../ui/StoreScreenshotsTest.kt`.

## 2026-10-10 · Avertissement Kotlin sur `@StringRes` (KT-73255)

- **Symptôme** : « This annotation is currently applied to the value parameter only, but in the future it will also be applied to field ».
- **Cause** : depuis Kotlin 2.2, la cible par défaut des annotations sur les propriétés de constructeur va changer.
- **Résolution** : cible explicite `@param:StringRes`.
- **Référence** : `apps/bruit-blanc/app/src/main/java/.../ui/NoiseScreen.kt`.

## 2026-10-10 · Hypothèse fausse sur le test fermé Google Play

- **Symptôme** : on pensait le compte Play antérieur au 13/11/2023, donc dispensé de test fermé. L'utilisateur a précisé avoir fait 14 jours de test fermé pour sa dernière app.
- **Cause** : le compte est un compte personnel créé après le 13/11/2023. Pour ces comptes, chaque app doit passer un test fermé de 14 jours consécutifs avec au moins 12 testeurs avant la production ([aide Google Play](https://support.google.com/googleplay/android-developer/answer/14151465)).
- **Résolution** : le plan prévoit un test fermé de 14 jours pour Bruit blanc ; les testeurs de licence installent l'app payante gratuitement. Le compte Play peut avoir une autre adresse e-mail que GitHub et GCP, sans conséquence.

## 2026-10-10 · Icône : la lune chevauchait les barres de son

- **Symptôme** : sur la bannière du Play Store, le bas de la lune touchait la plus haute barre.
- **Cause** : le croissant (cercle de rayon 14) descendait jusqu'à y = 58, alors que la barre centrale commençait à y = 55.
- **Résolution** : nouveau croissant calculé (cercles de centres (54,38) r13 et (60,33) r11), barres décalées vers le bas. Mêmes tracés dans l'icône de l'app et dans les visuels du Play Store.
- **Référence** : `apps/bruit-blanc/app/src/main/res/drawable/ic_launcher_foreground.xml`, `apps/bruit-blanc/store-art/`.

## 2026-10-10 · Rendu des visuels : ImageMagick ne lit pas le SVG

- **Symptôme** : `convert icon.svg icon.png` échoue (« no images defined »).
- **Cause** : l'ImageMagick de la session Claude n'a pas de module SVG.
- **Résolution** : les visuels sont écrits en HTML/SVG et rendus en PNG par Chromium via Playwright (`store-art/render.js`). Ce rendu est reproductible.
- **Référence** : `apps/bruit-blanc/store-art/`.

## 2026-10-10 · `setup-signing.sh` : « Project '511217' not found or permission denied »

- **Symptôme** : le script s'arrête à `gcloud services enable` dans Cloud Shell.
- **Cause** : on lui avait passé `511217`, un fragment de l'ID, au lieu de l'**ID du projet** complet (`android-app-factory-511217`).
- **Résolution** : récupérer l'ID avec `gcloud projects list` (colonne PROJECT_ID), puis relancer le script. Le commentaire en tête du script le précise désormais.
- **Référence** : `scripts/setup-signing.sh`.

## 2026-10-10 · Aucun projet GCP

- **Symptôme** : impossible de lancer le script de signature.
- **Cause** : le compte GCP existait, mais sans projet.
- **Résolution** : création du projet `android-app-factory-511217` dans la console, avec un compte de facturation (requis pour activer Secret Manager ; notre usage reste dans le quota gratuit).

## 2026-10-10 · Clé d'upload : pas de secrets GitHub, pas de mot de passe dans le chat

- **Symptôme** : (1) `gh secret set` échoue avec « HTTP 403: Access to this GitHub Actions path is not permitted through this proxy ». (2) La solution de repli, générer la clé dans la session et donner son mot de passe dans le chat, est refusée par les règles de sécurité de Claude (fuite d'identifiants).
- **Cause** : le proxy de la session bloque l'API des secrets GitHub, et un mot de passe de clé ne doit jamais transiter par une conversation. L'utilisateur ne peut rien installer sur sa machine.
- **Résolution** : la clé est générée dans **Google Cloud Shell** (navigateur, rien à installer) par `scripts/setup-signing.sh`, rangée dans **Secret Manager**, et lue par GitHub Actions via **Workload Identity Federation**, limitée à la branche `main` du dépôt. GitHub ne stocke ni clé ni mot de passe. Toute clé générée dans la session a été supprimée sans avoir été envoyée.
- **Référence** : `scripts/setup-signing.sh`, `.github/workflows/bruit-blanc.yml`.

## 2026-10-10 · Avertissements « Node.js 20 is deprecated » dans GitHub Actions

- **Symptôme** : avertissements sur `actions/checkout@v4` et `actions/setup-java@v4`.
- **Cause** : GitHub retire Node.js 20 des runners.
- **Résolution** : passage à `checkout@v5` et `setup-java@v5`. Il reste des avertissements non bloquants sur `setup-gradle@v4`, `upload-artifact@v4` et les actions Google, à mettre à jour quand leurs versions Node 24 seront confirmées.

## 2026-10-10 · Erreur de compilation : « Smart cast to 'Long' is impossible, because 'stopAt' is a delegated property »

- **Symptôme** : premier build GitHub Actions en échec sur `NoiseScreen.kt:124`.
- **Cause** : `state` est une propriété déléguée (`by collectAsStateWithLifecycle()`). Kotlin ne peut pas garantir que `state.stopAt` vaut la même chose entre le test `!= null` et son utilisation.
- **Résolution** : copier la valeur dans une variable locale (`val stopAt = state.stopAt`) avant de la tester.
- **Référence** : `apps/bruit-blanc/app/src/main/java/.../ui/NoiseScreen.kt`.

## 2026-10-10 · Pas de SDK Android ni de dépôt Maven Google dans la session Claude

- **Symptôme** : `dl.google.com` et `maven.google.com` renvoient 403 depuis la session.
- **Cause** : politique réseau de l'environnement de Claude.
- **Résolution** : tous les builds Android (compilation, tests, lint, signature) tournent dans GitHub Actions ; la session écrit le code et lit les journaux de build. Les versions des dépendances n'ont pas pu être vérifiées à l'avance, c'est le premier build qui les a validées.

## 2026-10-10 · Push refusé (403) sur `android-app-factory`

- **Symptôme** : « Claude doesn't have GitHub access to ELFACHTALI/android-app-factory ».
- **Cause** : l'app GitHub Claude n'était autorisée que sur certains dépôts du compte, et le nouveau dépôt n'en faisait pas partie.
- **Résolution** : ajout du dépôt dans les réglages de l'app GitHub Claude (github.com/apps/claude/installations/select_target).

## 2026-10-10 · Création de dépôts GitHub impossible depuis la session Claude

- **Symptôme** : la session Claude n'a pas le droit de créer des dépôts sur le compte GitHub.
- **Cause** : restriction de la session (elle n'agit que sur les dépôts qui lui sont rattachés).
- **Résolution** : l'utilisateur crée une seule fois `android-app-factory`. En production, c'est l'usine qui créera chaque dépôt d'app, via une GitHub App dédiée ; `bruit-blanc` sera le premier dépôt créé automatiquement.

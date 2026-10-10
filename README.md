# Android app factory

Usine d'apps Android multi-agents : on donne un nom et une idée d'app, des agents Claude écrivent l'app, la construisent avec GitHub Actions et la publient sur Google Play.

Proposition complète (outils, architecture, contraintes Play, plan du POC) : https://claude.ai/code/artifact/24405242-2270-478d-bcb0-40b23c798493

## Documentation

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) : ce qui existe, comment les pièces s'assemblent, chaîne de build et de signature.
- [docs/JOURNAL.md](docs/JOURNAL.md) : chaque erreur rencontrée, sa cause et sa résolution.
- [CLAUDE.md](CLAUDE.md) : consignes de travail pour Claude et les agents.

## Contenu

- `apps/bruit-blanc/` : l'app du POC (bruit blanc, rose et brun, sans pub, vendue 1 €). Elle sert de référence et de base au modèle d'app ; à terme l'usine créera chaque app dans son propre dépôt.
- `.github/workflows/bruit-blanc.yml` : tests, captures du Play Store, lint, build debug et bundle signé de l'app.
- `scripts/setup-signing.sh` : mise en place unique de la clé d'upload dans Secret Manager (Cloud Shell).
- Branche `store-listing` : fiche Play générée par la CI (textes, icône, bannière, captures), consultable sur GitHub.

## À venir

- `factory/` : service Cloud Run (entrée : nom de l'app) et job Cloud Run des agents (Claude Agent SDK).
- GitHub App de l'usine, pour créer les dépôts des apps automatiquement.
- Publication sur Google Play via l'API Play Developer.

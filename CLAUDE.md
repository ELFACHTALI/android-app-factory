# Consignes pour Claude et les agents de l'usine

- Le propriétaire écrit en français : documentation, commentaires de code et messages destinés à l'utilisateur en français.
- Documenter le code : KDoc sur chaque classe et fonction publique, commentaire sur toute ligne dont le pourquoi n'est pas évident.
- Chaque erreur rencontrée (build, CI, configuration, Play Console, GCP) est consignée dans `docs/JOURNAL.md` (symptôme, cause, résolution, référence), en haut du fichier, dans le même commit que la correction.
- Mettre à jour `docs/ARCHITECTURE.md` quand une brique change d'état ou qu'un fichier important apparaît.
- Les builds Android tournent dans GitHub Actions ; la session Claude n'a pas accès au SDK Android ni au dépôt Maven de Google.
- Aucune clé ni aucun mot de passe dans le dépôt, dans GitHub ou dans le chat : les secrets vivent dans Secret Manager (projet `android-app-factory-511217`).
- Demander l'accord du propriétaire avant toute action irréversible ou visible publiquement (publication en production, suppression).

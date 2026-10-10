# Play Console : réponses prêtes pour Bruit blanc

Ce que la Play Console demande avant le test fermé et la production, avec les réponses qui correspondent au code actuel. Google ne permet pas de remplir ces questionnaires par API : ils se remplissent à la main, une fois par app (menu « Contenu de l'application » sauf mention contraire).

## Règles de confidentialité

URL de la politique de confidentialité :
`https://github.com/ELFACHTALI/android-app-factory/blob/main/apps/bruit-blanc/PRIVACY.md`

## Sécurité des données

- L'app collecte-t-elle ou partage-t-elle des types de données utilisateur requis ? **Non.**
- Raison : aucune autorisation Internet dans le manifeste, aucun SDK tiers (pas d'analytics, pas de pub, pas de Firebase). Les réglages restent sur le téléphone.

## Publicités

- L'app contient-elle des annonces ? **Non.**

## Accès aux applications

- **Toutes les fonctionnalités sont disponibles sans restriction** (aucune connexion requise).

## Classification du contenu

- Catégorie : **Toutes les autres catégories d'applications** (utilitaire / bien-être).
- Violence, sexualité, langage grossier, substances, jeux d'argent, interactions entre utilisateurs, partage de position, achats numériques dans l'app : **Non** partout.
- Résultat attendu : PEGI 3 / Tout public.

## Public cible et contenu

- Tranches d'âge : **18 ans et plus** (recommandé). L'app sert aussi aux parents pour endormir un bébé, mais c'est l'adulte qui l'utilise ; cocher les tranches de moins de 13 ans soumettrait l'app au programme Familles et à ses règles supplémentaires.
- L'app peut-elle attirer involontairement des enfants ? **Non.**

## Services de premier plan (Autorisations > Services de premier plan)

- Type : **Lecture multimédia** (`mediaPlayback`).
- Description à coller : « L'utilisateur lance un son de fond (bruit blanc, rose ou brun) pour dormir ou se concentrer. La lecture doit continuer écran éteint et application fermée, jusqu'à ce que l'utilisateur l'arrête depuis la notification ou que la minuterie de sommeil se termine. »
- Vidéo : Google peut demander une courte vidéo montrant la fonction (lancer la lecture, verrouiller l'écran, le son continue, la notification avec le bouton Arrêt). Un enregistrement d'écran du téléphone suffit.

## Application d'actualité, gouvernement, finance, santé

- Actualités : **Non**. Application gouvernementale : **Non**. Fonctionnalités financières : **Aucune**. Applications de santé : **Non** (l'app ne fait aucune allégation médicale).

## Prix et pays (Monétiser > Produits > Prix de l'application)

- Prix : **1,00 €** (Google convertit pour les autres pays). Nécessite un profil de paiement lié au compte développeur.
- Les testeurs de licence (Paramètres > Tests de licence) installent l'app gratuitement pendant les tests.

## Test fermé (obligatoire pour ce compte)

- Piste « Test fermé » avec la liste « testing list » (au moins 12 testeurs inscrits), pendant **14 jours consécutifs**, avant de pouvoir demander l'accès à la production.

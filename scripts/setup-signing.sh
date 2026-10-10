#!/usr/bin/env bash
# Mise en place unique de la clé d'upload Play, à lancer dans Google Cloud Shell (rien à installer) :
#
#   git clone https://github.com/ELFACHTALI/android-app-factory && cd android-app-factory
#   bash scripts/setup-signing.sh ID_DU_PROJET_GCP     (l'ID texte, pas le numéro du projet)
#
# Le script génère la clé d'upload dans Cloud Shell et la range uniquement dans Secret Manager
# (elle n'est jamais affichée), puis autorise GitHub Actions, sur la branche main de ce dépôt
# seulement, à la lire via Workload Identity Federation : aucun secret n'est stocké dans GitHub.
# Il peut être relancé sans risque : les ressources existantes sont conservées.
set -euo pipefail

PROJECT_ID="${1:?Usage: bash scripts/setup-signing.sh PROJECT_ID}"
REPO="ELFACHTALI/android-app-factory"
APP="bruit-blanc"
POOL="github"
PROVIDER="github-actions"
SA_NAME="github-signing"
SA="$SA_NAME@$PROJECT_ID.iam.gserviceaccount.com"

gcloud config set project "$PROJECT_ID" >/dev/null
# androidpublisher : API Google Play Developer, utilisée par la CI pour publier.
gcloud services enable secretmanager.googleapis.com iam.googleapis.com \
  iamcredentials.googleapis.com sts.googleapis.com androidpublisher.googleapis.com
PROJECT_NUMBER="$(gcloud projects describe "$PROJECT_ID" --format='value(projectNumber)')"

# 1. Clé d'upload : générée ici, rangée dans Secret Manager, jamais affichée.
if ! gcloud secrets describe "$APP-upload-keystore" >/dev/null 2>&1; then
  TMP="$(mktemp -d)"
  trap 'rm -rf "$TMP"' EXIT
  PW="$(openssl rand -base64 48 | tr -dc 'A-Za-z0-9' | head -c 32)"
  keytool -genkeypair -keystore "$TMP/upload.jks" -storetype PKCS12 -alias upload \
    -keyalg RSA -keysize 4096 -validity 10000 -storepass "$PW" -keypass "$PW" \
    -dname "CN=$APP upload key" >/dev/null 2>&1
  gcloud secrets create "$APP-upload-keystore" --replication-policy=automatic --data-file="$TMP/upload.jks"
  printf %s "$PW" | gcloud secrets create "$APP-upload-password" --replication-policy=automatic --data-file=-
  unset PW
fi

# 2. Compte de service utilisé par GitHub Actions ; il ne peut lire que ces deux secrets.
gcloud iam service-accounts describe "$SA" >/dev/null 2>&1 \
  || gcloud iam service-accounts create "$SA_NAME" --display-name="GitHub Actions signing"
for secret in "$APP-upload-keystore" "$APP-upload-password"; do
  gcloud secrets add-iam-policy-binding "$secret" --member="serviceAccount:$SA" \
    --role=roles/secretmanager.secretAccessor >/dev/null
done

# 3. Workload Identity Federation, limitée à la branche main de ce dépôt.
gcloud iam workload-identity-pools describe "$POOL" --location=global >/dev/null 2>&1 \
  || gcloud iam workload-identity-pools create "$POOL" --location=global --display-name="GitHub"
gcloud iam workload-identity-pools providers describe "$PROVIDER" --location=global \
  --workload-identity-pool="$POOL" >/dev/null 2>&1 \
  || gcloud iam workload-identity-pools providers create-oidc "$PROVIDER" --location=global \
    --workload-identity-pool="$POOL" --display-name="GitHub Actions" \
    --issuer-uri="https://token.actions.githubusercontent.com" \
    --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository,attribute.ref=assertion.ref" \
    --attribute-condition="assertion.repository=='$REPO' && assertion.ref=='refs/heads/main'"
gcloud iam service-accounts add-iam-policy-binding "$SA" --role=roles/iam.workloadIdentityUser \
  --member="principalSet://iam.googleapis.com/projects/$PROJECT_NUMBER/locations/global/workloadIdentityPools/$POOL/attribute.repository/$REPO" \
  >/dev/null

echo
echo "Done. Send these two lines to Claude (they are not secret):"
echo "WIF_PROVIDER=projects/$PROJECT_NUMBER/locations/global/workloadIdentityPools/$POOL/providers/$PROVIDER"
echo "SERVICE_ACCOUNT=$SA"

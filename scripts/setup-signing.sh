#!/usr/bin/env bash
# One-time setup of the Play upload key, run in Google Cloud Shell (nothing to install):
#
#   git clone https://github.com/ELFACHTALI/android-app-factory && cd android-app-factory
#   bash scripts/setup-signing.sh YOUR_GCP_PROJECT_ID
#
# It generates the upload key inside Cloud Shell and stores it only in Secret Manager
# (never printed), then lets GitHub Actions on the main branch of this repository read it
# through Workload Identity Federation, so no key or password is stored in GitHub.
# Safe to re-run: existing resources are kept.
set -euo pipefail

PROJECT_ID="${1:?Usage: bash scripts/setup-signing.sh PROJECT_ID}"
REPO="ELFACHTALI/android-app-factory"
APP="bruit-blanc"
POOL="github"
PROVIDER="github-actions"
SA_NAME="github-signing"
SA="$SA_NAME@$PROJECT_ID.iam.gserviceaccount.com"

gcloud config set project "$PROJECT_ID" >/dev/null
gcloud services enable secretmanager.googleapis.com iam.googleapis.com \
  iamcredentials.googleapis.com sts.googleapis.com
PROJECT_NUMBER="$(gcloud projects describe "$PROJECT_ID" --format='value(projectNumber)')"

# 1. Upload key: generated here, stored in Secret Manager, never shown.
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

# 2. Service account that GitHub Actions impersonates; it can only read these two secrets.
gcloud iam service-accounts describe "$SA" >/dev/null 2>&1 \
  || gcloud iam service-accounts create "$SA_NAME" --display-name="GitHub Actions signing"
for secret in "$APP-upload-keystore" "$APP-upload-password"; do
  gcloud secrets add-iam-policy-binding "$secret" --member="serviceAccount:$SA" \
    --role=roles/secretmanager.secretAccessor >/dev/null
done

# 3. Workload Identity Federation, limited to the main branch of this repository.
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

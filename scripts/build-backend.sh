#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
OUT_DIR="$ROOT_DIR/backend-build"
BACKEND_DIR="$ROOT_DIR/backend"

cd "$BACKEND_DIR"

./mvnw package -DskipTests

mkdir -p "$OUT_DIR"

for module in document-rest audit-service grpc-analytics-server grpc-enrichment-client notification-service; do
  mkdir -p "$OUT_DIR/$module"
  cp "$module"/target/*.jar "$OUT_DIR/$module/"
done

printf 'Backend jars copied to %s\n' "$OUT_DIR"

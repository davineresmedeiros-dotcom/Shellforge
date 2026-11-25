#!/bin/sh
# Shellforge Gradle launcher. Downloads the pinned Gradle distribution on first run.
set -eu

GRADLE_VERSION=8.9
DIST_NAME="gradle-${GRADLE_VERSION}-bin"
USER_HOME="${GRADLE_USER_HOME:-${HOME}/.gradle}"
DIST_DIR="${USER_HOME}/wrapper/dists/${DIST_NAME}"
GRADLE_BIN="${DIST_DIR}/gradle-${GRADLE_VERSION}/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$DIST_DIR"
  ZIP_FILE="${DIST_DIR}/${DIST_NAME}.zip"
  URL="https://services.gradle.org/distributions/${DIST_NAME}.zip"
  echo "Downloading Gradle ${GRADLE_VERSION}..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL --retry 3 "$URL" -o "$ZIP_FILE"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP_FILE" "$URL"
  else
    echo "curl or wget is required to download Gradle." >&2
    exit 1
  fi
  if command -v unzip >/dev/null 2>&1; then
    unzip -q -o "$ZIP_FILE" -d "$DIST_DIR"
  else
    echo "unzip is required to extract Gradle." >&2
    exit 1
  fi
  rm -f "$ZIP_FILE"
fi
exec "$GRADLE_BIN" "$@"

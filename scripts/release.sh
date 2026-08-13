#!/usr/bin/env bash
set -euo pipefail

# Release tag script: checkout main, pull, tag with user-provided label, push tag

echo "Checking out main..."
git checkout main

echo "Pulling latest..."
git pull

versionLineArr=($(grep "version = " build.gradle.kts))
TAG_LABEL=$(echo "${versionLineArr[2]}" | tr -d '"')

echo "Creating tag: $TAG_LABEL"
git tag "$TAG_LABEL"

echo "Pushing tag $TAG_LABEL..."
git push origin "$TAG_LABEL"

echo "Done. Tag $TAG_LABEL has been created and pushed."

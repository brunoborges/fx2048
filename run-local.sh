#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$repo_dir"

./mvnw javafx:jlink javafx:run \
  -Djavafx.executable="$repo_dir/target/fx2048/bin/java"

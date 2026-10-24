#!/usr/bin/env bash
set -euo pipefail
project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir/frontend"
if [[ ! -d node_modules ]]; then npm ci; fi
exec npm run dev

#!/usr/bin/env bash
set -euo pipefail
project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir/backend"
if [[ ! -f .env ]]; then
  echo "Copy backend/.env.example to backend/.env and configure MySQL/JWT first."
  exit 1
fi
set -a
source .env
set +a
: "${DB_USERNAME:?Set DB_USERNAME}" "${DB_PASSWORD:?Set DB_PASSWORD}" "${JWT_SECRET:?Set JWT_SECRET}"
exec sh ./mvnw spring-boot:run

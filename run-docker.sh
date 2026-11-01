#!/usr/bin/env bash
# Faculty command: start MongoDB and the API together with Docker.
set -euo pipefail
cd "$(dirname "$0")/.."
if [ ! -f .env ]; then cp .env.example .env; echo "Created .env from .env.example. Edit it before using real data."; fi
docker compose up --build

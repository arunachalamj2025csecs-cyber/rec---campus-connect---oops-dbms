#!/usr/bin/env bash
# Faculty command: check that the API is answering. Usage: scripts/health-check.sh [API_URL]
URL="${1:-http://localhost:8080}"
curl -fsS "$URL/api/health" && echo && echo "API is running." || { echo "API is NOT reachable at $URL"; exit 1; }

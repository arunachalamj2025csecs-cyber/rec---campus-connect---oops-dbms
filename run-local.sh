#!/usr/bin/env bash
# Faculty command: start the Java API on this computer (MongoDB must be running).
set -euo pipefail
cd "$(dirname "$0")/../backend"
if [ -f ../.env ]; then set -a; . ../.env; set +a; fi
mvn -B spring-boot:run

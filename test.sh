#!/usr/bin/env bash
# Faculty command: run only the automated tests.
set -euo pipefail
cd "$(dirname "$0")/../backend"
mvn -B test

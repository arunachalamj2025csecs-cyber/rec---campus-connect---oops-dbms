#!/usr/bin/env bash
# Faculty command: build the Java backend and run all tests.
set -euo pipefail
cd "$(dirname "$0")/../backend"
mvn -B clean verify
echo "Build finished. The jar is in backend/target/campus-connect-api.jar"

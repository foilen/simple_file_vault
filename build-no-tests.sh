#!/bin/bash

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$RUN_PATH"

# Set version
if [ -z "$VERSION" ]; then
    export VERSION=$(git rev-parse --abbrev-ref HEAD)-SNAPSHOT
fi

# Build
./gradlew clean bootBuildImage -x test -PappVersion=$VERSION --stacktrace

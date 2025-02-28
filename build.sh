#!/bin/bash

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$RUN_PATH"

# Set version
if [ -z "$1" ]; then
    export VERSION=$(git rev-parse --abbrev-ref HEAD)-SNAPSHOT
else
    export VERSION=$1
fi

# Build
./gradlew clean bootBuildImage -PappVersion=$VERSION --stacktrace

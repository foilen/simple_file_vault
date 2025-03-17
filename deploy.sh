#!/bin/bash

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

# Set version
if [ -z "$1" ]; then
    export VERSION=$(git rev-parse --abbrev-ref HEAD)-SNAPSHOT
else
    export VERSION=$1
fi

# Upload to docker hub
DOCKER_IMAGE=simple_file_vault:$VERSION
docker login
docker tag $DOCKER_IMAGE foilen/$DOCKER_IMAGE
docker tag $DOCKER_IMAGE foilen/simple_file_vault:latest
docker push foilen/$DOCKER_IMAGE
docker push foilen/simple_file_vault:latest

# Tag the version
git tag -a -m $VERSION $VERSION

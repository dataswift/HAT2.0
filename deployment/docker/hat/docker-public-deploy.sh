#!/bin/bash

set -e

REPOSITORY_NAME=${REPOSITORY_NAME:-hubofallthings}

VERSION=${HAT_VERSION:-$(git log --format="%H" -n 1)}
APPLICATION_NAME="hat"

echo "Build ${APPLICATION_NAME}:${VERSION}"
sbt "project ${APPLICATION_NAME}" docker:stage

echo "Create package"
cd ${APPLICATION_NAME}/target/docker/stage
docker build -t ${REPOSITORY_NAME}/${APPLICATION_NAME}:${VERSION} .
docker push ${REPOSITORY_NAME}/${APPLICATION_NAME}:${VERSION}

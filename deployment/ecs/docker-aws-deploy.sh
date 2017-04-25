#!/bin/bash

set -e

BUCKET=${AWS_BUCKET:-hat-cloud-formation-eu}
VERSION=`git log --format="%H" -n 1`

echo "Create package"
export APPLICATION_NAME="hat"
HAT_HOME=${PWD} #if executing from deployment/  : "$PWD/../.."
DOCKER=${DOCKER:-"${HAT_HOME}/deployment/docker"}
bash ${DOCKER}/hat/docker-build.sh

echo "Publish to AWS"
docker tag hubofallthings/${APPLICATION_NAME}:latest 717711705314.dkr.ecr.eu-west-1.amazonaws.com/hubofallthings:${VERSION}
docker push 717711705314.dkr.ecr.eu-west-1.amazonaws.com/hubofallthings:${VERSION}

echo "Built application version ${APPLICATION_NAME} ${VERSION}"
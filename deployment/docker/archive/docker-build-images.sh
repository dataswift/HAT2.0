#!/usr/bin/env bash

HAT_HOME=${HAT_HOME:-"$PWD"} #if executing from deployment/  : "$PWD/../.."
DOCKER=${DOCKER:-"${HAT_HOME}/deployment/docker"}

bash ${DOCKER}/hat/docker-build.sh
#bash ${DOCKER}/hat-postgres/docker-build.sh

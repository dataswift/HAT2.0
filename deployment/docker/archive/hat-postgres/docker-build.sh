#!/usr/bin/env bash
set -e

DATABASE=${DATABASE:-"hat20"}
DBUSER=${DBUSER:-$DATABASE}
DBPASS=${DBPASS:-"hat20"}
#tipically docker folder is in HAT2.0/deployment/docker
HAT_HOME=${HAT_HOME:-"$PWD"} #if executing from deployment/  : "$PWD/../.."
DOCKER=${DOCKER:-"$PWD/deployment/docker/hat-postgres"}
DOCKER_DEPLOY=${DOCKER}/docker-deploy

echo "Creating $DOCKER_DEPLOY"
mkdir ${DOCKER_DEPLOY}
mkdir ${DOCKER_DEPLOY}/required

echo "Copying required files"
cp -r ${HAT_HOME}/hat-database-schema/*.sql ${DOCKER_DEPLOY}/required/
cp -r ${HAT_HOME}/hat-database-schema/*.sql.template ${DOCKER_DEPLOY}/required/
cp -r ${HAT_HOME}/hat-database-schema/setupAccess.sh ${DOCKER_DEPLOY}/required/

cp -r ${DOCKER}/init ${DOCKER_DEPLOY}/init
cp ${DOCKER}/Dockerfile ${DOCKER_DEPLOY}/Dockerfile

#echo "Building db docker image: docker-hat-postgres"
docker build -t hubofallthings/hat-postgres ${DOCKER_DEPLOY}

echo "Cleaning up"
rm -r ${DOCKER_DEPLOY}

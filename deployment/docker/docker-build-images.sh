#!/usr/bin/env bash

DATABASE=${DATABASE:-"hat20"}
DBUSER=${DBUSER:-$DATABASE}
DBPASS=${DBPASS:-"hat20"}
#tipically docker folder is in HAT2.0/deployment/docker
HAT_HOME=${HAT_HOME:-"$PWD"} #if executing from deployment/  : "$PWD/../.."
DOCKER=${DOCKER:-"$PWD/deployment/docker"}
DOCKER_DEPLOY=$DOCKER/docker-deploy

echo "Creating $DOCKER_DEPLOY"
mkdir $DOCKER_DEPLOY
mkdir $DOCKER_DEPLOY/required

echo "Copying required files"
cp $DOCKER/docker-deploy-db.sh $DOCKER_DEPLOY/
cp $DOCKER/database.conf.template $DOCKER_DEPLOY/required/database.conf
cp $DOCKER/database.conf.template $HAT_HOME/src/main/resources/database.conf
cp $HAT_HOME/src/main/resources/database.conf $HAT_HOME/codegen/src/main/resources/database.conf
cp $HAT_HOME/src/sql/* $DOCKER_DEPLOY/required
cp $HAT_HOME/src/sql/boilerplate/* $DOCKER_DEPLOY/required
cp  $DOCKER/docker-entrypoint.sh $DOCKER_DEPLOY/

echo "Setting up HAT access"
HAT_OWNER=${HAT_OWNER:-'bob@gmail.com'}
HAT_OWNER_ID=${HAT_OWNER_ID:-5974832d-2dc1-4f49-adf1-c6d8bc790274}
HAT_OWNER_NAME=${HAT_OWNER_NAME:-'Bob'}
HAT_OWNER_PASSWORD=${HAT_OWNER_PASSWORD:-'pa55w0rd'}
HAT_PLATFORM=${HAT_PLATFORM:-'hatdex.org'}
HAT_PLATFORM_ID=${HAT_PLATFORM_ID:-47dffdfd-55e8-4575-836c-151e30bb5a50}
HAT_PLATFORM_NAME=${HAT_PLATFORM_NAME:-'hatdex'}
HAT_PLATFORM_PASSWORD_HASH=${HAT_PLATFORM_PASSWORD_HASH:-'$2a$04$oL2CXTHzB..OekL1z8Vijus3RkHQjSsbkAYOiA5Rj.7.6GA7a4qAq'}

echo "Generating PG Dockerfile"
cat $DOCKER/Dockerfile-pg.template | sed -e "s/%DATABASE%/$DATABASE/g"\
	-e "s/%DBUSER%/$DBUSER/g"\
	-e "s/%DBPASS%/$DBPASS/g"\
	-e "s/%HAT_OWNER%/$HAT_OWNER/g"\
	-e "s/%HAT_OWNER_ID%/$HAT_OWNER_ID/g"\
	-e "s/%HAT_OWNER_NAME%/$HAT_OWNER_NAME/g"\
	-e "s/%HAT_OWNER_PASSWORD%/$HAT_OWNER_PASSWORD/g"\
	-e "s/%HAT_PLATFORM%/$HAT_PLATFORM/g"\
	-e "s/%HAT_PLATFORM_ID%/$HAT_PLATFORM_ID/g"\
	-e "s/%HAT_PLATFORM_NAME%/$HAT_PLATFORM_NAME/g"\
	-e "s/%HAT_PLATFORM_PASSWORD_HASH%/$HAT_PLATFORM_PASSWORD_HASH/g"\
	> $DOCKER_DEPLOY/Dockerfile

echo "Building HAT : sbt docker:stage"
sbt docker:stage

if [ ! -f "$HAT_HOME/target/docker/Dockerfile" ]; then
    echo "Missing $HAT_HOME/target/docker/Dockerfile" 
    echo "The docker-hat container was not created."
    echo "Please run 'sbt docker:stage' on main folder and re-run this script to generate it."
    exit
fi

cd $DOCKER_DEPLOY

echo "Building db docker image: docker-hat-postgres"
docker build -t hubofallthings/hat-postgres .

cp -r $HAT_HOME/target/docker/stage/opt $DOCKER_DEPLOY/
#Save current postgres docker image
mv $DOCKER_DEPLOY/Dockerfile $DOCKER_DEPLOY/Dockerfile-hatpg

touch $DOCKER_DEPLOY/Dockerfile
echo "#Do not modify this file. Use Dockerfile-hat.template instead." > $DOCKER_DEPLOY/Dockerfile
cat $DOCKER/Dockerfile-hat.template >> $DOCKER_DEPLOY/Dockerfile

echo "Building hat docker image: docker-hat"
docker build -t hubofallthings/hat .

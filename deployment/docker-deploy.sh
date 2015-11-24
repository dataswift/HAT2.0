#!/usr/bin/env bash

DATABASE=${DATABASE:-"hat20"}
DBUSER=${DBUSER:-$DATABASE}
DBPASS=${DBPASS:-"hat20"}
HAT_HOME=${HAT_HOME:-"$PWD/.."}
DOCKER_DEPLOY=$HAT_HOME/deployment/docker-deploy

echo "Creating $DOCKER_DEPLOY"
mkdir $DOCKER_DEPLOY
mkdir $DOCKER_DEPLOY/required

echo "Copying required files"
cp $HAT_HOME/deployment/docker-deploy-db.sh $DOCKER_DEPLOY/
#TODO: remove the lines below, just put the file there directly
sed -e "s;%DATABASE%;$DATABASE;g" -e "s;%DBUSER%;$DBUSER;g" -e "s;%DBPASS%;$DBPASS;g" -e "s;%SERVERNAME%;hat-postgres-$HAT_OWNER_NAME;g" $HAT_HOME/deployment/database.conf.template > $DOCKER_DEPLOY/required/database.conf
cp $HAT_HOME/src/sql/* $DOCKER_DEPLOY/required
cp  $HAT_HOME/deployment/docker-entrypoint.sh $DOCKER_DEPLOY/


echo "Setting up HAT access"
HAT_OWNER='bob@gmail.com'
HAT_OWNER_ID=5974832d-2dc1-4f49-adf1-c6d8bc790274
HAT_OWNER_NAME='Bob'
HAT_OWNER_PASSWORD='pa55w0rd'
HAT_PLATFORM=${HAT_PLATFORM:-'hatdex.org'}
HAT_PLATFORM_ID=${HAT_PLATFORM_ID:-47dffdfd-55e8-4575-836c-151e30bb5a50}
HAT_PLATFORM_NAME=${HAT_PLATFORM_NAME:-'hatdex'}
HAT_PLATFORM_PASSWORD_HASH=${HAT_PLATFORM_PASSWORD_HASH:-'$2a$04$oL2CXTHzB..OekL1z8Vijus3RkHQjSsbkAYOiA5Rj.7.6GA7a4qAq'}

echo "Generating PG Dockerfile"
cat Dockerfile-pg.template | sed -e "s/%DATABASE%/$DATABASE/g"\
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

cd $DOCKER_DEPLOY

echo "Building db docker image: docker-hat-postgres"
docker build -t docker-hat-postgres .

echo "Creating docker-hat-postgres run script"
echo "docker run -d --name hat-postgres-$HAT_OWNER_NAME docker-hat-postgres" > $DOCKER_DEPLOY/run-db.sh

if [ ! -f "$HAT_HOME/target/docker/Dockerfile" ]; then
    echo "Missing $HAT_HOME/target/docker/Dockerfile" 
    echo "The docker-hat container was not created."
    echo "Please run 'sbt docker:stage' on main folder and re-run this script to generate it."
    exit
fi

echo "Building hat docker image:"
#sbt -sbt-dir $HAT_HOME docker:stage
cp -r $HAT_HOME/target/docker/stage/opt $DOCKER_DEPLOY/
#Save current postgres docker image
mv $DOCKER_DEPLOY/Dockerfile $DOCKER_DEPLOY/Dockerfile-hatpg

touch $DOCKER_DEPLOY/Dockerfile
echo "#Do not modify this file. Use Dockerfile-hat.template instead." > $DOCKER_DEPLOY/Dockerfile
cat ../Dockerfile-hat.template >> $DOCKER_DEPLOY/Dockerfile
#cp $HAT_HOME/target/docker/stage/Dockerfile $DOCKER_DEPLOY/

docker build -t docker-hat .

docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

cd $DOCKER_DEPLOY
echo "Creating docker-hat run script"
#-d = background ; --name
echo "docker run -d --name hat-$HAT_OWNER_NAME docker-hat" > $DOCKER_DEPLOY/run-hat.sh
#sudo chmod +x run-hat.sh

# echo "Launching docker-hat-postgres..."
# docker run -d --name hat-postgres-$HAT_OWNER_NAME docker-hat-postgres

# echo "Launching docker-hat container..."
# docker run -d --name hat-$HAT_OWNER_NAME --link hat-postgres-$HAT_OWNER_NAME -p 3000:8080 docker-hat

#Test postgres
#psql -h 172.17.0.2 -p 5432 -d hat20 -U hat20 --password

# sleep 60

# echo "Boilerplate setup"
# echo "PGPASSWORD=$DBPASS psql -h $pg -p 5432 -d $DATABASE -U $DBUSER"
# PGPASSWORD=$DBPASS psql -h $pg -p 5432 -d $DATABASE -U $DBUSER < $HAT_HOME/src/sql/data.sql
# PGPASSWORD=$DBPASS psql -h $pg -p 5432 -d $DATABASE -U $DBUSER < $HAT_HOME/src/sql/relationships.sql
# PGPASSWORD=$DBPASS psql -h $pg -p 5432 -d $DATABASE -U $DBUSER < $HAT_HOME/src/sql/properties.sql
# PGPASSWORD=$DBPASS psql -h $pg -p 5432 -d $DATABASE -U $DBUSER < $HAT_HOME/src/sql/collections.sql
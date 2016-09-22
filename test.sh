#!/bin/bash

cd ./hat-database-schema
source ./env.sh
bash ./setupDatabase.sh
bash ./setupAccess.sh
bash ./applyEvolutions.sh -c structuresonly,testdata
cd ../
sed -e "s;%DATABASE%;$DATABASE;g" -e "s;%DBUSER%;$DBUSER;g" -e "s;%DBPASS%;$DBPASS;g" deployment/database.conf.template > src/main/resources/database.conf
cp ./src/main/resources/database.conf ./codegen/src/main/resources/database.conf
sbt "testOnly hatdex.hat.api.*" -Dconfig.file=src/main/resources/application.test.conf
dropdb $DATABASE
dropuser $DBUSER

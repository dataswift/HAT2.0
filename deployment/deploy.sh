#!/usr/bin/env bash

DATABASE=${DATABASE:-"hat20"}
DBUSER=${DBUSER:-$DATABASE}
DBPASS=${DBPASS:-"pa55w0rd"}
HAT_HOME=${HAT_HOME:-"."}

#export PGUSER=postgres #force the user to be postgres, to ensure permissions in following commands

# Create the DB
# NOSUPERUSER NOCREATEDB NOCREATEROLE
echo "Setting up database user and database"
echo "dbuser: $DBUSER, database: $DATABASE"
createuser -S -D -R -e $DBUSER
createdb $DATABASE -O $DBUSER
psql $DATABASE -c "ALTER USER $DBUSER WITH PASSWORD '$DBPASS';"

#DBUSER wouldnt have required permissions to drop/create public schema otherwise
echo "Handling schemas"
psql $DATABASE -c 'DROP SCHEMA public CASCADE;'
psql $DATABASE -c 'CREATE SCHEMA public;'
psql $DATABASE -c "ALTER SCHEMA public OWNER TO $DBUSER;"

echo "Setting up database"
psql $DATABASE -c 'CREATE EXTENSION "uuid-ossp";'
psql $DATABASE -c 'CREATE EXTENSION "pgcrypto";'

export PGPASSWORD="$DBPASS" #use the given password for the next psql commands with user DBUSER

# Execute HAT-V2.0 SQL script
psql $DATABASE -U$DBUSER < $HAT_HOME/src/sql/HAT-V2.0.sql
echo $HAT_HOME/src/sql/HAT-V2.0.sql

# Setup db config for the project
echo "Setting up corresponding configuration"
sed -e "s;%DATABASE%;$DATABASE;g" -e "s;%DBUSER%;$DBUSER;g" -e "s;%DBPASS%;$DBPASS;g" $HAT_HOME/deployment/database.conf.template > $HAT_HOME/src/main/resources/database.conf
cp $HAT_HOME/src/main/resources/database.conf $HAT_HOME/codegen/src/main/resources/database.conf

# Setup HAT access
echo "Setting up HAT access"
HAT_OWNER=${HAT_OWNER:-'bob@gmail.com'}
HAT_OWNER_ID=${HAT_OWNER_ID:-5974832d-2dc1-4f49-adf1-c6d8bc790274}
HAT_OWNER_NAME=${HAT_OWNER_NAME:-'Bob'}
HAT_OWNER_PASSWORD=${HAT_OWNER_PASSWORD:-'pa55w0rd'}

HAT_PLATFORM=${HAT_PLATFORM:-'hatdex.org'}
HAT_PLATFORM_ID=${HAT_PLATFORM_ID:-47dffdfd-55e8-4575-836c-151e30bb5a50}
HAT_PLATFORM_NAME=${HAT_PLATFORM_NAME:-'hatdex'}
HAT_PLATFORM_PASSWORD_HASH=${HAT_PLATFORM_PASSWORD_HASH:-'$2a$04$oL2CXTHzB..OekL1z8Vijus3RkHQjSsbkAYOiA5Rj.7.6GA7a4qAq'}

sed -e "s;%HAT_OWNER%;$HAT_OWNER;g"\
  -e "s;%HAT_OWNER_ID%;$HAT_OWNER_ID;g"\
  -e "s;%HAT_OWNER_NAME%;$HAT_OWNER_NAME;g"\
  -e "s;%HAT_OWNER_PASSWORD%;$HAT_OWNER_PASSWORD;g"\
  -e "s;%HAT_PLATFORM%;$HAT_PLATFORM;g"\
  -e "s;%HAT_PLATFORM_ID%;$HAT_PLATFORM_ID;g"\
  -e "s;%HAT_PLATFORM_NAME%;$HAT_PLATFORM_NAME;g"\
  -e "s;%HAT_PLATFORM_PASSWORD_HASH%;$HAT_PLATFORM_PASSWORD_HASH;g"\
  $HAT_HOME/src/sql/boilerplate/authentication.sql.template > $HAT_HOME/src/sql/boilerplate/authentication.sql

# Execute the sql script
psql $DATABASE -U$DBUSER < $HAT_HOME/src/sql/boilerplate/authentication.sql

# Remove the sql file with sensitive credentials
rm $HAT_HOME/src/sql/boilerplate/authentication.sql

echo "Boilerplate setup"
echo $HAT_HOME/src/sql/boilerplate/data.sql
psql $DATABASE -U$DBUSER < $HAT_HOME/src/sql/boilerplate/data.sql
psql $DATABASE -U$DBUSER < $HAT_HOME/src/sql/boilerplate/relationships.sql
psql $DATABASE -U$DBUSER < $HAT_HOME/src/sql/boilerplate/properties.sql
psql $DATABASE -U$DBUSER < $HAT_HOME/src/sql/boilerplate/collections.sql

# Prepare the project to be executed in-place
echo "Preparing the project to be executed"
#sbt clean
#sbt compile
sbt stage

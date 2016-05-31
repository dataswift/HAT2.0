#!/usr/bin/env bash

#THIS SCRIPT SHOULD EXECUTE WITHIN THE DOCKER POSTGRES IMAGE!

DATABASE=${DATABASE:-"hat20"}
DBUSER=${DBUSER:-$DATABASE}
DBPASS=${DBPASS:-"hat20"}
#In case we are not executing the deploy from the repo (e.g., in container)
HAT_HOME=${HAT_HOME:-".."}

export POSTGRES_PASSWORD=$DBPASS
export POSTGRES_USER=$DBUSER
export POSTGRES_DB=$DATABASE
export PGUSER=postgres

# Create the DB
# NOSUPERUSER NOCREATEDB NOCREATEROLE
#echo "Setting up DB role"
#createuser -S -D -R -e $DBUSER
#echo "Setting up role DB"
#createdb $DATABASE -O $DBUSER

#DBUSER wouldnt have required permissions to drop/create public schema otherwise
echo "Handling schemas"
psql -c 'DROP SCHEMA public CASCADE;'
psql -c 'CREATE SCHEMA public;'
psql -c "ALTER SCHEMA public OWNER TO $DBUSER;"

echo "Setting up database"
psql $DATABASE -c 'CREATE EXTENSION "uuid-ossp";'
psql $DATABASE -c 'CREATE EXTENSION "pgcrypto";'
psql $DATABASE -U$DBUSER < $HAT_HOME/HAT-V2.0.sql

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
  $HAT_HOME/authentication.sql.template > $HAT_HOME/authentication.sql

# Execute the sql script
psql $DATABASE -U$DBUSER < $HAT_HOME/authentication.sql

# Remove the sql file with sensitive credentials
rm $HAT_HOME/authentication.sql

echo "Boilerplate setup"
psql $DATABASE -U$DBUSER < $HAT_HOME/data.sql
psql $DATABASE -U$DBUSER < $HAT_HOME/relationships.sql
psql $DATABASE -U$DBUSER < $HAT_HOME/properties.sql
psql $DATABASE -U$DBUSER < $HAT_HOME/collections.sql

env
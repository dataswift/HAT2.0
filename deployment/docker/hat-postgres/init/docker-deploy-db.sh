#!/usr/bin/env bash

#THIS SCRIPT SHOULD EXECUTE WITHIN THE DOCKER POSTGRES IMAGE!

DATABASE=${DATABASE:-"hat20"}
DBUSER=${DBUSER:-$DATABASE}
DBPASS=${DBPASS:-"hat20"}
#In case we are not executing the deploy from the repo (e.g., in container)
HAT_HOME=${HAT_HOME:-"/opt/hat"}

export POSTGRES_PASSWORD=$DBPASS
export POSTGRES_USER=$DBUSER
export POSTGRES_DB=$DATABASE
export PGUSER=postgres

#DBUSER wouldnt have required permissions to drop/create public schema otherwise
echo "Setting up database"
psql ${DATABASE} < ${HAT_HOME}/01_init.sql

echo "Setting up main schema"
psql ${DATABASE} -U$DBUSER < ${HAT_HOME}/11_hat.sql

echo "Setting up evolutions (without running the evolutions engine)"
psql ${DATABASE} -U$DBUSER < ${HAT_HOME}/12_hatEvolutions.sql
psql ${DATABASE} -U$DBUSER < ${HAT_HOME}/13_liveEvolutions.sql


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

cd ${HAT_HOME}
bash ./setupAccess.sh

# Execute the sql script
psql $DATABASE -U$DBUSER < ${HAT_HOME}/41_authentication.sql

# Remove the sql file with sensitive credentials
rm ${HAT_HOME}/41_authentication.sql

echo "Boilerplate setup"
psql $DATABASE -U$DBUSER < ${HAT_HOME}/31_properties.sql
psql $DATABASE -U$DBUSER < ${HAT_HOME}/32_relationships.sql
psql $DATABASE -U$DBUSER < ${HAT_HOME}/33_staticData.sql
psql $DATABASE -U$DBUSER < ${HAT_HOME}/35_sampleCollections.sql

env

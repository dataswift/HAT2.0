#!/usr/bin/env bash

DATABASE=${DATABASE:-"hat20test"}
DBUSER=${DBUSER:-$DATABASE}
DBPASS=${DBPASS:-""}


# Create the DB
# NOSUPERUSER NOCREATEDB NOCREATEROLE
echo "Setting up database user and database"
createuser -S -D -R -e $DBUSER
createdb $DATABASE -O $DBUSER
psql $DATABASE -c 'CREATE EXTENSION "uuid-ossp";'
psql $DATABASE -c 'CREATE EXTENSION "pgcrypto";'
psql $DATABASE -U$DBUSER < src/sql/HAT-V2.0.sql

# Setup db config for the project
echo "Setting up corresponding configuration"
sed -e "s;%DATABASE%;$DATABASE;g" -e "s;%DBUSER%;$DBUSER;g" -e "s;%DBPASS%;$DBPASS;g" deployment/database.conf.template > src/main/resources/database.conf
cp src/main/resources/database.conf codegen/src/main/resources/database.conf

# Setup HAT access
echo "Setting up HAT access"
HAT_OWNER='bob@gmail.com'
HAT_OWNER_ID=5974832d-2dc1-4f49-adf1-c6d8bc790274
HAT_OWNER_NAME='Bob'
HAT_OWNER_PASSWORD='pa55w0rd'

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
  src/sql/authentication.sql.template > src/sql/authentication.sql

# Execute the sql script
psql $DATABASE -U$DBUSER < src/sql/authentication.sql

# Remove the sql file with sensitive credentials
rm src/sql/authentication.sql

echo "Boilerplate setup"
psql $DATABASE -U$DBUSER < src/sql/data.sql
psql $DATABASE -U$DBUSER < src/sql/relationships.sql
psql $DATABASE -U$DBUSER < src/sql/properties.sql
psql $DATABASE -U$DBUSER < src/sql/collections.sql

# Rebuild and run the project
echo "Compiling and running the project"
#sbt clean
#sbt compile
sbt run
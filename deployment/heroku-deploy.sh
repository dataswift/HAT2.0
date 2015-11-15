#!/bin/bash
cd ..

# create app
heroku create hatdemo

# create db
heroku addons:create heroku-postgresql:hobby-dev

# setup db
heroku pg:psql --app hatdemo < src/sql/HAT-V2.0.sql

heroku pg:psql -c "CREATE EXTENSION pgcrypto;"
pg:psql --app hatdemo < src/sql/extensions.sql

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
heroku pg:psql --app hatdemo < src/sql/authentication.sql

# Remove the sql file with sensitive credentials
rm src/sql/authentication.sql

echo "Boilerplate setup"
heroku pg:psql --app hatdemo < src/sql/data.sql
heroku pg:psql --app hatdemo < src/sql/relationships.sql
heroku pg:psql --app hatdemo < src/sql/properties.sql
heroku pg:psql --app hatdemo < src/sql/collections.sql

cp deployment/database.heroku.conf src/main/resources/database.conf
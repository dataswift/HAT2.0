#!/bin/bash
cf create-service ElephantSQL turtle hatdb
cp manifest.yml ..
cp run.sh ..
cd ..
cf push thehat

DBURL=$(cf env thehat \
 | grep '"uri": "postgres' \
 | sed 's/[ \t]* "uri"://g' \
 | tr -d " \t\n\r\"")

psql $DBURL < src/sql/HAT-V2.4.sql

rm manifest.yml
rm run.sh
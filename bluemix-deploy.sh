#!/bin/bash
cf create-service ElephantSQL turtle hatdb
cf push thehat

DBURL=$(cf env thehat \
 | grep '"uri": "postgres' \
 | sed 's/[ \t]* "uri"://g' \
 | tr -d " \t\n\r\"")

psql $DBURL < src/sql/HAT-V2.4.sql
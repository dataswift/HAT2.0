#!/bin/bash
cd ..
heroku pg:psql --app hatdemo < src/sql/HAT-V2.0.sql

#!/bin/bash

# add send to user

export ADJUDICATOR_ADDRESS="https://contracts.hubat.net"
export AWS_REGION="eu-west-1"
export APPLICATION_CACHE_TTL="10 seconds"
export DEX_ADDRESS="https://dex.dataswift.io"
export DROPS_SHE_BASE_URL="drops-she-sandbox"
export REDIS_HOST="hatstagingredis-0001-001.onquve.0001.euw1.cache.amazonaws.com:6379"
export HAT_ADMIN_EMAIL="systems@vault.dataswift.dev"
export HAT_BETA="true"
export HAT_DB_IDLE_TIMEOUT="60 seconds"
export HAT_DB_THREADS="15"
export HAT_DOMAIN=".hubat.net"
export HAT_SERVER_PROVIDER="org.hatdex.hat.modules.HatServerProviderModule"
export HAT_STORAGE_S3_BUCKET="dataswift-sandbox-pds-eu-hat"
export JAVA_OPTS="-Xmx1000m -Xms512m"
export MAILER_FROM="Dataswift <systems@eu.vault.dataswift.dev>"
export MILLINER_ADDRESS="https://milliner.hubat.net"
export PDA_REGISTRY_HOST="https://hatters.dataswift.io"
export RESOURCE_MGMT_SERVER_IDLE_TIMEOUT="180 seconds"
export SHE_BASE_URL="dswift-she-sandbox"
export DBUSER=masteruser
export DBPASS=YFgbCbN8PWIilIJmMINv
export DATABASE=postgresql://dataswift-staging-pds-eu-hat-n1.cdgbpyaamcol.eu-west-1.rds.amazonaws.com:5432/hatAdmin
export POSTGRES_PORT=5432


sbt "project hat" run -Denv=prod


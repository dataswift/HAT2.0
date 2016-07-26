![Hub of All things](http://hubofallthings.com/wp-content/uploads/banner21.png)

[![Build Status](https://travis-ci.org/Hub-of-all-Things/HAT2.0.svg?branch=master)](https://travis-ci.org/Hub-of-all-Things/HAT2.0)
[![Coverage Status](https://coveralls.io/repos/Hub-of-all-Things/HAT2.0/badge.svg?branch=master&service=github)](https://coveralls.io/github/Hub-of-all-Things/HAT2.0?branch=master)

# HAT 2.0

This repository contains an implementation of the [Hub-of-All-Things](http://hubofallthings.com) project Personal Data Store.

## Releases

The current project version is a pre-release [HAT 2.0](https://github.com/Hub-of-all-Things/HAT2.0/releases/tag/v2.0).


## About the project

Hub-of-All-Things is a platform for a Multi-Sided Market powered by the Internet of Things.

A Personal Data Management System (“the HAT”) is a personal single tenant (“the individual self”) technology system that is fully individual self-service, to enable an individual to define a full set of “meta-data” defining as a specific set of personal data, personal preferences, personal behaviour events. The HAT allows individuals to share the right information (quality and quantity), with the right people, in the right situations for the right purposes and gain the benefits.

[![PhD Comics explains the HAT](http://img.youtube.com/vi/y1txYjoSQQc/0.jpg)](http://www.youtube.com/watch?v=y1txYjoSQQc)

## Technology stack

This HAT PDS implementation is written in Scala (2.11.6) uses the following technology stack:

- PostgreSQL relational database (version 9.4)
- Spray REST/HTTP toolkit (version 1.3.3)
- Akka (version 2.3.9)
- Slick as the database access layer (version 2.1)

## Running the project


### HAT Setup

If you use docker, the official HAT can be configured with a number of environment variables and instantiated as two docker containers: one for the database and one for the core HAT:

    name=myhat

    docker run\
      -e "DATABASE=$name"\
      -e "DBUSER=$name"\
      -e "DBPASS=$name"\
      -e "POSTGRES_PASSWORD=$name"\
      -e "POSTGRES_USER=$name"\
      -e "POSTGRES_DB=$name"\
      -e "HAT_OWNER=$name@gmail.com"\
      -e "HAT_OWNER_NAME=$name"\
      -e "HAT_OWNER_PASSWORD=$name"\
      -d --name hat-postgres-$name hubofallthings/hat-postgres

    docker run\
      -e "DATABASE=$name"\
      -e "DBUSER=$name"\
      -e "DBPASS=$name"\
      -e "POSTGRES_PASSWORD=$name"\
      -e "POSTGRES_USER=$name"\
      -e "POSTGRES_DB=$name"\
      -e "HAT_OWNER=$name@gmail.com"\
      -e "HAT_OWNER_NAME=$name"\
      -e "HAT_OWNER_PASSWORD=$name"\
      -d --name hat-$name --link hat-postgres-$name -p $port:8080 hubofallthings/hat

The following variables are available:

- `HAT_HOME` - path [default: current (".")]
- `DATABASE` - name of the database [default: hat20test]
- `DBUSER` - database username [default: hat20test]
- `DBPASS` - database user password [default: pa55w0rd]
- `HAT_OWNER` - HAT owner identity (DNS name/username) [default: bob@gmail.com]
- `HAT_OWNER_ID` - HAT owner GUID
- `HAT_OWNER_NAME` - HAT owner name [default: Bob]
- `HAT_OWNER_PASSWORD` - HAT owner login password [default: pa55w0rd]
- `HAT_PLATFORM` - HAT platform identity [default: hatdex.org]
- `HAT_PLATFORM_ID` - HAT platform GUID
- `HAT_PLATFORM_NAME` - HAT platform name [default: hatdex]
- `HAT_PLATFORM_PASSWORD_HASH` - BCrypt-hashed HAT platform password for platform-management operations (application account creation only)

If you do not wish to use Docker, you can execute the script at `deployment/deploy.sh` to run the HAT locally, using the environment variables for configuration

### Run the project!
Execute the following command:

    ./target/universal/stage/bin/the-hat

**You're all set!**

Now you will be able to access `127.0.0.1:8080` or `localhost:8080` in your browser, and see the message "Hello HAT 2.0!".

You can customise `host` and `port` settings by passing the right configuration parameters, for example:

    ./target/universal/stage/bin/the-hat -DapplicationHost=0.0.0.0 -DapplicationPort=10080

### Testing

Functionality is tested via the provided tests. To run this code, setup [sbt](http://www.scala-sbt.org) and run

	sbt test
	
The testsuite uses the [Specs2](https://etorreborre.github.io/specs2/) framework.

*WARNING*: the tests completely clean the contents of the database you are connected to, multiple times. *DO NOT RUN on production database.*

### Remote HAT Testing

For various purposes from performance testing to stress testing, to deployed HAT Api verification we have also included a testsuite for testing remote HATs using normal HTTP calls.

To run it, use

  sbt "testOnly hatdex.hat.api.external.RemoteApiSpec" -Dhost="HOST" -Dusername="USERNAME" -Dpassword="PASSWORD"

with the appropriate parameters

### API

API documentation can be found at [http://hub-of-all-things.github.io/doc/](http://hub-of-all-things.github.io/doc/) 

### Additional information

- [Environment Setup Guides](https://github.com/Hub-of-all-Things/HAT2.0/wiki/Environment-Setup)
- [Common Problems](https://github.com/Hub-of-all-Things/HAT2.0/wiki/Common-Problems)
- [Auto-generated Database interface for Slick](https://github.com/Hub-of-all-Things/HAT2.0/wiki/Auto-generated-Database-interface-for-Slick)
- [Packaging and Running](https://github.com/Hub-of-all-Things/HAT2.0/wiki/Packaging-and-running)

## License

[HAT including HAT Schema and API] is licensed under [AGPL - GNU AFFERO GENERAL PUBLIC LICENSE](https://github.com/Hub-of-all-Things/HAT/blob/master/LICENSE/AGPL)

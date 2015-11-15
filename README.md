![Hub of All things](http://hubofallthings.com/wp-content/uploads/banner21.png)

[![Build Status](https://travis-ci.org/Hub-of-all-Things/HAT2.0.svg?branch=master)](https://travis-ci.org/Hub-of-all-Things/HAT2.0)

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

### Environment setup

You will need to install PostgreSQL, SBT (Scala Build Tool), and Java 8.

If you use Ubuntu, you can use the following commands:

    # Install PostgreSQL
    sudo apt-get install postgresql postgresql-client postgresql-contrib
    sudo apt-get update
    
    # Install SBT (Scala Build Tool)
    echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823
    sudo apt-get update
    sudo apt-get install sbt
    
    # Install Java 8
    sudo apt-get install software-properties-common
    sudo add-apt-repository ppa:webupd8team/java
    sudo apt-get update
    sudo apt-get install oracle-java8-installer

### Clone the HAT project

Then, you will be able to clone the repository:

    git clone https://github.com/Hub-of-all-Things/HAT2.0.git hat

### Database Setup

You will need to set up a PostgreSQL database with the HAT2.0 schema, configure the project to use the database, and prepare it to be executed. You can do all that by executing the following script:

    ./deployment/deploy.sh
    
The provided script executes all required commands to get the project running, and can be configured through environment variables. For example, you can run:

    HAT_HOME=".." DATABASE=mynewHAT DBUSER=mynewuser DBPASS=mynewpass ./deployment/deploy.sh

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

If you do not specify those variables, take a look at the deploy.sh script to see the default values.

### Run the project!
Execute the following command:

    sbt run

**You're all set!**

Now you will be able to access `127.0.0.1:8080` or `localhost:8080` in your browser, and see the message "Hello HAT 2.0!".

### Common problems

Here we list common problems you may have encountered in this process.

#### 1. Database peer authentication failed at the top

You may have seen an error message similar to `psql: FATAL:  Peer authentication failed for user "hat20"`.

Your PostgreSQL installation is configured to use peer authentication, which means that the user in your operating system and postgres must be the same.

You can change it either to **md5** (password-based authentication) or **trust** (anyone who can connect to the server can access the database).

[This thread in Stack Overflow](http://stackoverflow.com/questions/18664074/getting-error-peer-authentication-failed-for-user-postgres-when-trying-to-ge) explains in more detail your options and implications.

You will need to edit the file `pg_hba.conf` (`/etc/postgresql/9.3/main/pg_hba.conf`, depending on your postgres version), and change the **method**. For example:

    # TYPE  DATABASE        USER            ADDRESS                 METHOD
    local   all             postgres                                md5
    local   all             all                                     md5


#### 2. Another program is already listening to port 8080

You can specify another port or host directly in the run command. For example:

    sbt run -DapplicationHost=YOUR_IP -DapplicationPort=9090

### Auto-generated code recompilation

You should have the right code for database access already in your code, but if you want to regenerate it if the project database structure changes, you can rerun it with:

    sbt

Then within the sbt console:
    
    project codegen
    clean
    gentables

*Note*: you should never need to do this.

This uses Slick's code auto-generation feature, where the necessary code to interface with the database gets generated from the provided SQL database structure.

### Testing

Functionality is tested via the provided tests. To run this code, setup [sbt](http://www.scala-sbt.org) and run

	sbt test
	
The testsuite uses the [Specs2](https://etorreborre.github.io/specs2/) framework.

*WARNING*: the tests completely clean the contents of the database you are connected to, multiple times. *DO NOT RUN on production database.*

### API

The API code lives in `src/main/scala/hatdex/hat/dalapi`

Swagger.io based API documentation can be found at [http://hub-of-all-things.github.io/api/](http://hub-of-all-things.github.io/api/) 

### Packaging and running

The code can be packaged using standard sbt commands, e.g.

    sbt stage
    
or

    sbt docker:stage
    
For running in respective environments

The `deployment` directory provides simple scripts and configuration for running on Heroku and IBM BlueMix

## TODO

- contextual bundle implementation and testing
- contextless bundle data ​grouping/_joins_​ (otherwise complete)
- entity api testing
- finished documentation of all APIs
- more detailed documentation of the schema


## License

This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. To view a copy of this license, visit [http://creativecommons.org/licenses/by-nc-nd/4.0/](http://creativecommons.org/licenses/by-nc-nd/4.0/) or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
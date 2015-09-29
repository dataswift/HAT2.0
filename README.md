![Hub of All things](http://hubofallthings.com/wp-content/uploads/banner21.png)

# HAT 2.0

This repository contains an implementation of the [Hub-of-All-Things](http://hubofallthings.com) project Personal Data Store.

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

### Database Setup

You will need to set up a PostgreSQL database with the HAT2.0 schema, files for which can be found in `src/sql`. E.g.:

    psql databasename < src/sql/HAT-V2.0.sql

Configuration in *both* `codegen/src/main/resources/database.conf` and `src/main/resources/database.conf` must reflect your database configuration (can be different ones for model generation and for operation), and look similar to:

    devdb = {
      dataSourceClass = "org.postgresql.ds.PGSimpleDataSource"
      properties = {
        databaseName = "database"
        user = "dbuser"
        password = "dbpass"
      }
    }

### Auto-generated code recompilation

You should have the right code for database access already in your code, but if you want to regenerate it if the project database structure changes, you can rerun it with:

    sbt

Then within the sbt console:

    clean
    gen-tables

*Note*: simply running `sbt clean gen-tables` does not tend to regenerate the tables

This uses Slick's code auto-generation feature, where the necessary code to interface with the database gets generated from the provided SQL database structure.

### Testing

Functionality is tested via the provided tests. To run this code, setup [sbt](http://www.scala-sbt.org) and run

	sbt test
	
The testsuite uses the [Specs2](https://etorreborre.github.io/specs2/) framework.

*WARNING*: the tests completely clean the contents of the database you are connected to, multiple times. *DO NOT RUN on production database.*

### API

The API code lives in `src/main/scala/hatdex/hat/dalapi`

API documentation can be found at: 

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
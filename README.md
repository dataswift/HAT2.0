[![Build Status](https://travis-ci.org/Hub-of-all-Things/HAT2.0.svg?branch=master)](https://travis-ci.org/Hub-of-all-Things/HAT2.0)
<!--[![Coverage Status](https://coveralls.io/repos/Hub-of-all-Things/HAT2.0/badge.svg?branch=master&service=github)](https://coveralls.io/github/Hub-of-all-Things/HAT2.0?branch=master)-->

# [Hub of All Things](https://hubofallthings.com)

This repository contains an implementation of the [Hub-of-All-Things](http://hubofallthings.com) HAT Microserver
project.

## Releases

The current project version is [here](https://github.com/dataswift/HAT2.0/releases).

## About the project

The Hub-of-All-Things is a HAT Microserver for individuals to own, control and share their data.

A Personal Microserver (“the HAT”) is a personal single tenant (“the individual self”) technology system that is fully
individual self-service, to enable an individual to define a full set of “meta-data” defined as a specific set of
personal data, personal preferences and personal behaviour events.

The HAT enables individuals to share the correct information (quality and quantity), with the correct people, in the
correct situations for the correct purposes and to gain the benefits.

The HAT microserver is the technology base of the Persona Data Server.

## Technology stack

This HAT Microserver implementation is written in Scala (2.12.11) uses the following technology stack:

- [PostgreSQL](https://www.postgresql.org) relational database (version 9.5 and above)
- [Play Framework](https://www.playframework.com) (version 2.6)
- [Akka](https://akka.io) (version 2.5)
- [Slick](https://scala-slick.org/) as the database access layer (version 3.2)

## Running the HAT project

HAT runs as a combination of a backing PostgreSQL database (with a
[public schema](https://github.com/Hub-of-all-Things/hat-database-schema)
for flattened data storage) and a software stack that provides logic to work with the schema using HTTP APIs.

To run it from source in a development environment two sets of tools are required:

- PostgreSQL database and utilities
- [Scala Build Tool](https://www.scala-sbt.org) (SBT)

### 1. Get the Source and the submodules for both of the methods

    > git clone https://github.com/Hub-of-all-Things/HAT2.0.git
    > cd HAT2.0
    > git submodule init 
    > git submodule update

### 2. Configure your /etc/hosts

    127.0.0.1   bobtheplumber.hat.org
    127.0.0.1   bobtheplumber.example.com

### 3. Create the database

There are 2 ways of doing this.

#### 3.1. Using your local postgresql instance
```bash
    > createdb testhatdb1
    > createuser testhatdb1
    > psql postgres -c "GRANT CREATE ON DATABASE testhatdb1 TO testhatdb1"
```

#### 3.2. Using `docker-compose`
A docker-compose.yml file has been included in this project to boot up a dockerized postgresql instance.
If you do use this method, you need to create a `.env` file. The included `.env.example` will work as is.
If you make a change, do make the corresponding change to `./hat/conf/dev.conf`
Then
```bash
    > make docker-db
```

You can stop the database with
```bash
    > make docker-db-stop
```

### 4. Run the project!

```bash
    > make run-dev
```
Go to [http://bobtheplumber.example.com:9000](http://bobtheplumber.example.com:9000)


**You're all set!**

### Customising your development environment

Your best source of information on how the development environment could be customised is the `hat/conf/dev.conf`
configuration file. Make sure you run the project locally with the configuration enabled (using the steps above)
or it will just show you the message that the HAT could not be found.

Among other things, the configuration includes:

- host names alongside port numbers of the test HATs ([http://yourname.hat.org:9000](http://yourname.hat.org:9000))
- access credentials used to log in as the owner or restricted platform user into the HAT (the default password is a
  very unsafe *testing*)
- database connection details (important if you want to change your database setup above)
- private and public keys used for token signing and verification

Specifically, it has 4 major sections:

- Enables the development environment self-initialisation module:
    ```
    play.modules {
      enabled += "org.hatdex.hat.modules.DevHatInitializationModule"
    }
    ```
- Sets the list of database evolutions to be executed on initialisation:
    ```
    devhatMigrations = [
      "evolutions/hat-database-schema/11_hat.sql",
      "evolutions/hat-database-schema/12_hatEvolutions.sql",
      "evolutions/hat-database-schema/13_liveEvolutions.sql",
      "evolutions/hat-database-schema/14_newHat.sql"]
    ```  
- `devhats` list sets out the list of HATs that are served by the current server, for each including owner details to be
  initialised with and database access credentials. Each specified database must exist before launching the server but
  are initialised with the right schema at start time
- `hat` section lists all corresponding HAT configurations to serve, here you could change the HAT domain name, owner's
  email address or public/private keypair used by the HAT for its token operations

## Using Helm 3

The HAT solution is easy deployable on top of Kubernetes via [Helm 3 chart](charts).

## Additional information

- API documentation can be found at the [developers' portal](https://developers.hubofallthings.com)
- [HAT Database Schema](https://github.com/Hub-of-all-Things/hat-database-schema) has been split up into a separate
  project for easier reuse across different environments.

## License

HAT including HAT Schema and API is licensed
under [AGPL - GNU AFFERO GENERAL PUBLIC LICENSE](https://github.com/Hub-of-all-Things/HAT/blob/master/LICENSE/AGPL)

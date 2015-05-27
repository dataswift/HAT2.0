HAT 2.0
=======

Starting afresh with the new schema...

Starting with a fairly auto-generated data access layer for the HAT

You will need to set up a PostgreSQL database with the HAT2.2 schema, files for which can be found in `src/sql`.

Configuration in *both* `codegen/src/main/resources/application.conf` and `src/main/resources/application.conf` must reflect your database configuration (can be different ones for model generation and for operation), and look similar to:

    devdb = {
      dataSourceClass = "org.postgresql.ds.PGSimpleDataSource"
      properties = {
        databaseName = "database"
        user = "dbuser"
        password = "dbpass"
      }
    }

To run this code, setup activator and simply type

	activator test

Which will run tests checking the db. All tests should finish successfully.
	
package dalapi

import com.typesafe.config.ConfigFactory
import dal.SlickPostgresDriver.simple._

trait DatabaseInfo {
  val conf = ConfigFactory.load()
  val dbconfig = conf.getString("applicationDb")
  val db = Database.forConfig(dbconfig)
}
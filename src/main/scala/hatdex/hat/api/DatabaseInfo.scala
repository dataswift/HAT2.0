package hatdex.hat.api

import com.typesafe.config.ConfigFactory
import hatdex.hat.dal.SlickPostgresDriver.simple._

object DatabaseInfo {
  val conf = ConfigFactory.load()
  val dbconfig = conf.getString("applicationDb")
  val db = Database.forConfig(dbconfig)
}
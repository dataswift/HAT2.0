package hatdex.hat.api.actors

import akka.actor.{ Props, ActorLogging, ActorRefFactory, Actor }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.HttpsConnectionContext

import akka.http.scaladsl.model._
import akka.stream.{ ActorMaterializer, Materializer }
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import hatdex.hat.api.actors.StatsReporter.{ PostStats, StatsMessageQueued }
import hatdex.hat.authentication.JwtTokenHandler
import akka.http.scaladsl.model.headers.RawHeader
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.Tables._
import hatdex.hat.dal.SlickPostgresDriver.api._
import org.joda.time.{ LocalDateTime, LocalDate }
import spray.json._
import hatdex.hat.api.{ DatabaseInfo, Api }
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.stats._
import hatdex.hat.api.service.StatsService
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.pipe

object StatsReporter {
  def props: Props = Props[StatsReporter]

  case class StatsMessageQueued(timestamp: LocalDateTime, retries: Int, message: DataStats)
  case class PostStats()
  case class ComputeStorageStats(tableId: Option[Int])
}

class StatsReporter extends Actor with ActorLogging with JwtTokenHandler {
  import StatsReporter._
  val logger = Logging.getLogger(context.system, "Stats")
  logger.info("Stats Reporter Actor starting")

  val exchangeUri = conf.getString("exchange.uri")
  val retryLimit = conf.getInt("exchange.retryLimit")
  val retryTime = conf.getDuration("exchange.retryTime")
  val statsBatchSize = conf.getInt("exchange.batchSize")
  val storageStatsPeriod = conf.getDuration("exchange.storage.collectionPeriod")

  implicit val system = context.system
  val badSslConfig = AkkaSSLConfig()

  lazy val platformUser: Future[User] = {
    val userQuery = UserUser.filter(_.role === "platform").filter(_.enabled === true).take(1)
    val matchingUsers = DatabaseInfo.db.run(userQuery.result)
    matchingUsers.map { users =>
      users.headOption.map(User.fromDbModel).get
    }
  }

  var statsQueue = mutable.Queue[StatsMessageQueued]()

  val reportingScheudle = context.system.scheduler
    .schedule(0 millis, FiniteDuration(retryTime.toMillis, "millis"), self, PostStats())
  val storedDatacollectionSchedule = context.system.scheduler
    .schedule(10 seconds, FiniteDuration(storageStatsPeriod.toMillis, "millis"), self, ComputeStorageStats(None))

  def receive: Receive = {
    case stats: DataDebitStats =>
      statsQueue += StatsMessageQueued(LocalDateTime.now(), 0, stats)
    case stats: DataCreditStats =>
      statsQueue += StatsMessageQueued(LocalDateTime.now(), 0, stats)
    case stats: DataStorageStats =>
      logger.info(s"Data storage stats computed: $stats")
      statsQueue += StatsMessageQueued(LocalDateTime.now(), 0, stats)
    case ComputeStorageStats(tableId) =>
      computeDataStorage(tableId) pipeTo sender
    case PostStats() =>
      logger.info(s"Trying to post stats")
      postStats()
  }

  import JsonProtocol._
  import spray.json.CollectionFormats
  implicit val materializer: Materializer = ActorMaterializer()
  def postStats() = {
    val fToken = for {
      user <- platformUser
      token <- fetchOrGenerateToken(user, exchangeUri)
    } yield token

    fToken map { authToken =>
      val statsExpired = statsQueue.dequeueAll(_.retries >= retryLimit)
      if (statsExpired.length > 0) {
        logger.warning(s"Stats reporting expired for ${statsExpired.length} stats: ${statsExpired.map(_.message).mkString("\n")}")
      }

      val stats = statsQueue.dequeueAll(_.retries < retryLimit)

      for (i <- 0 to (stats.length / statsBatchSize)) {
        val statsBatch = stats.slice(i * statsBatchSize, Math.min((i + 1) * statsBatchSize, stats.length)).toList

        if (statsBatch.nonEmpty) {
          val request = HttpRequest(HttpMethods.POST, Uri(exchangeUri).withPath(Uri.Path(s"/stats/report")))
            .withHeaders(RawHeader("X-Auth-Token", authToken.accessToken))
            .withEntity(HttpEntity(MediaTypes.`application/json`, statsBatch.map(_.message).toJson.toString))

          val result = Http().singleRequest(request).map(_.status) map {
            case StatusCodes.OK =>
              logger.info(s"Stats successfully posted")
            case statusCode =>
              logger.error(s"Error while posting stats: $statusCode")
              statsQueue ++= statsBatch.map(message => message.copy(retries = message.retries + 1))
          } recover {
            case e =>
              logger.error(s"Stats could not be posted: ${e.getMessage}")
              statsQueue ++= statsBatch.map(message => message.copy(retries = message.retries + 1))
          }
        }
      }
    }
  }

  def computeDataStorage(tableId: Option[Int] = None): Future[DataStorageStats] = {
    val fieldRecordCounts = DataValue.groupBy(v => v.fieldId)
      .map { case (fieldId, values) =>
        (fieldId, values.map(_.recordId).countDistinct)
      }

    val fields = if (tableId.isDefined) {
      fieldRecordCounts.filter(_._1 === tableId.get)
    }
    else {
      fieldRecordCounts
    }

    val dataTableStats = DatabaseInfo.db.run {
      fields.result
    } flatMap { results =>
      val fieldsOfInterest = Map(results: _*)

      val tableFields = for {
        field <- DataField.filter(_.id inSet fieldsOfInterest.keys)
        table <- field.dataTableFk
      } yield (field, table)

      DatabaseInfo.db.run {
        tableFields.result
      } map { results =>
        results map { case (field: DataFieldRow, table: DataTableRow) =>
          val dfStats = DataFieldStats(field.name, table.name, table.sourceName, fieldsOfInterest.getOrElse(field.id, 0))
          val dtStats = DataTableStats(table.name, table.sourceName, Seq(), None, 0)
          (dtStats, dfStats)
        }
      }
    } map { statsSequence =>
      statsSequence.groupBy(_._1).map {
        case (table, fields) => // Aggregate counts by table, 1 level deep
          table.copy(fields = fields.map(_._2), valueCount = fields.map(_._2.valueCount).sum)
      }
    }

    dataTableStats map { stats =>
      DataStorageStats(LocalDateTime.now(), stats.toSeq, "Data Storage Statistics")
    }
  }
}

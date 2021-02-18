// /*
//  * Copyright (C) 2017 HAT Data Exchange Ltd
//  * SPDX-License-Identifier: AGPL-3.0
//  *
//  * This file is part of the Hub of All Things project (HAT).
//  *
//  * HAT is free software: you can redistribute it and/or modify
//  * it under the terms of the GNU Affero General Public License
//  * as published by the Free Software Foundation, version 3 of
//  * the License.
//  *
//  * HAT is distributed in the hope that it will be useful, but
//  * WITHOUT ANY WARRANTY; without even the implied warranty of
//  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
//  * the GNU Affero General Public License for more details.
//  *
//  * You should have received a copy of the GNU Affero General
//  * Public License along with this program. If not, see
//  * <http://www.gnu.org/licenses/>.
//  *
//  * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>
//  * 7 / 2018
//  */

// package org.hatdex.hat.api.service.richData

// import org.hatdex.hat.api.models._
// import org.joda.time.DateTime
// import org.specs2.concurrent.ExecutionEnv
// import org.specs2.mock.Mockito
// import org.specs2.specification.{ BeforeAll, BeforeEach }
// import play.api.Logger
// import play.api.test.PlaySpecification

// import scala.concurrent.Await
// import scala.concurrent.duration._

// class DataSentintelSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with RichDataServiceContext with BeforeEach with BeforeAll {

//   val logger = Logger(this.getClass)

//   sequential

//   def beforeAll: Unit = {
//     Await.result(databaseReady, 60.seconds)
//   }

//   override def before: Unit = {
//     import org.hatdex.hat.dal.Tables._
//     import org.hatdex.libs.dal.HATPostgresProfile.api._

//     val endpointRecrodsQuery = DataJson.filter(_.source.like("test%")).map(_.recordId)

//     val action = DBIO.seq(
//       DataDebitBundle.filter(_.bundleId.like("test%")).delete,
//       DataDebitContract.filter(_.dataDebitKey.like("test%")).delete,
//       DataCombinators.filter(_.combinatorId.like("test%")).delete,
//       DataBundles.filter(_.bundleId.like("test%")).delete,
//       DataJsonGroupRecords.filter(_.recordId in endpointRecrodsQuery).delete,
//       DataJsonGroups.filterNot(g => g.groupId in DataJsonGroupRecords.map(_.groupId)).delete,
//       DataJson.filter(r => r.recordId in endpointRecrodsQuery).delete)

//     Await.result(hatDatabase.run(action), 60.seconds)
//   }

//   "The `ensureUniquenessKey` method" should {
//     "Correctly extract item ID from data" in {
//       val dataService = application.injector.instanceOf[RichDataService]
//       val service = application.injector.instanceOf[DataSentintel]

//       val data = List(
//         EndpointData("test/test", None, None, None, simpleJson, None),
//         EndpointData("test/test", None, None, None, simpleJson2, None))

//       val result = for {
//         saved <- dataService.saveData(owner.userId, data)
//         _ <- service.ensureUniquenessKey("test/test", "date")
//         retrieved <- dataService.propertyData(List(EndpointQuery("test/test", None, None, None)), None, false, 0, None)
//       } yield retrieved

//       result map { result =>
//         result.length must equalTo(2)
//         result(0).sourceUniqueId must beSome("1492699047")
//         result(1).sourceUniqueId must beSome("1492799048")
//       } await (3, 10.seconds)
//     }

//     "Delete duplicate records for clashing source IDs, retaining newer record" in {
//       val dataService = application.injector.instanceOf[RichDataService]
//       val service = application.injector.instanceOf[DataSentintel]

//       val data = List(
//         EndpointData("test/test", None, None, None, simpleJson, None),
//         EndpointData("test/test", None, None, None, simpleJson2, None))

//       val result = for {
//         _ <- dataService.saveData(owner.userId, data)
//         _ <- dataService.saveData(owner.userId, List(EndpointData("test/test", None, None, None, simpleJson2Updated, None)))
//         _ <- service.ensureUniquenessKey("test/test", "date")
//         retrieved <- dataService.propertyData(List(EndpointQuery("test/test", None, None, None)), None, false, 0, None)
//       } yield retrieved

//       result map { result =>
//         result.length must equalTo(2)
//         result.find(_.sourceUniqueId.contains("1492699047")) must beSome
//         result.find(_.sourceUniqueId.contains("1492799048")) must beSome
//         (result(1).data \ "differentField").as[String] must equalTo("new")
//       } await (3, 10.seconds)
//     }

//     "Not touch records where extracting ID fails" in {
//       val dataService = application.injector.instanceOf[RichDataService]
//       val service = application.injector.instanceOf[DataSentintel]

//       val data = List(
//         EndpointData("test/test", None, None, None, simpleJson, None),
//         EndpointData("test/test", None, None, None, simpleJson2, None))

//       val result = for {
//         _ <- dataService.saveData(owner.userId, data)
//         _ <- dataService.saveData(owner.userId, List(EndpointData("test/test", None, None, None, simpleJson2Updated, None)))
//         _ <- service.ensureUniquenessKey("test/test", "testUniqueID")
//         retrieved <- dataService.propertyData(List(EndpointQuery("test/test", None, None, None)), None, false, 0, None)
//       } yield retrieved

//       result map { result =>
//         result.find(_.sourceUniqueId.contains("1234567")) must beSome
//         result.count(r => (r.data \ "date").asOpt[Int].contains(1492799048)) must equalTo(2)
//       } await (3, 10.seconds)
//     }

//     "Handle records where ID is nested deeply within the object" in {
//       val dataService = application.injector.instanceOf[RichDataService]
//       val service = application.injector.instanceOf[DataSentintel]

//       val data = List(EndpointData("test/test", None, None, None, simpleJson, None))

//       val result = for {
//         _ <- dataService.saveData(owner.userId, data)
//         _ <- service.ensureUniquenessKey("test/test", "object.nestedInfo.deeplyLocatedUniqueId")
//         retrieved <- dataService.propertyData(List(EndpointQuery("test/test", None, None, None)), None, false, 0, None)
//       } yield retrieved

//       result map { result =>
//         result.find(_.sourceUniqueId.contains("7654321")) must beSome
//       } await (3, 10.seconds)
//     }

//     "Not update records when key is specified to be within an array" in {
//       val dataService = application.injector.instanceOf[RichDataService]
//       val service = application.injector.instanceOf[DataSentintel]

//       val data = List(EndpointData("test/test", None, None, None, simpleJson, None))

//       val result = for {
//         _ <- dataService.saveData(owner.userId, data)
//         _ <- service.ensureUniquenessKey("test/test", "object.objectFieldArray[]")
//         retrieved <- dataService.propertyData(List(EndpointQuery("test/test", None, None, None)), None, false, 0, None)
//       } yield retrieved

//       result map { result =>
//         result.forall(_.sourceUniqueId.isEmpty) must beTrue
//       } await (3, 10.seconds)
//     }
//   }

//   "The `ensureUniquenessKey` method" should {
//     "Correctly extract ISO8601 timestamp from data" in {
//       val dataService = application.injector.instanceOf[RichDataService]
//       val service = application.injector.instanceOf[DataSentintel]

//       val data = List(EndpointData("test/test", None, None, None, simpleJson, None))

//       val result = for {
//         _ <- dataService.saveData(owner.userId, data)
//         _ <- service.updateSourceTimestamp("test/test", "date_iso")
//         retrieved <- dataService.propertyData(List(EndpointQuery("test/test", None, None, None)), None, false, 0, None)
//       } yield retrieved

//       result map { result =>
//         result.head.sourceTimestamp must beSome
//         result.head.sourceTimestamp.get.isEqual(DateTime.parse("2017-04-20T14:37:27+00:00")) must beTrue
//       } await (3, 10.seconds)
//     }

//     "Correctly extract unix timestamp in milliseconds from data" in {
//       val dataService = application.injector.instanceOf[RichDataService]
//       val service = application.injector.instanceOf[DataSentintel]

//       val data = List(EndpointData("test/test", None, None, None, simpleJson, None))

//       val result = for {
//         _ <- dataService.saveData(owner.userId, data)
//         _ <- service.updateSourceTimestamp("test/test", "date_ms")
//         retrieved <- dataService.propertyData(List(EndpointQuery("test/test", None, None, None)), None, false, 0, None)
//       } yield retrieved

//       result map { result =>
//         result.head.sourceTimestamp must beSome
//         result.head.sourceTimestamp.get.isEqual(DateTime.parse("2017-04-20T14:37:27+00:00")) must beTrue
//       } await (3, 10.seconds)
//     }

//     "Correctly extract unix timestamp in miliseconds from data" in {
//       val dataService = application.injector.instanceOf[RichDataService]
//       val service = application.injector.instanceOf[DataSentintel]

//       val data = List(EndpointData("test/test", None, None, None, simpleJson, None))

//       val result = for {
//         _ <- dataService.saveData(owner.userId, data)
//         _ <- service.updateSourceTimestamp("test/test", "date", "'epoch'")
//         retrieved <- dataService.propertyData(List(EndpointQuery("test/test", None, None, None)), None, false, 0, None)
//       } yield retrieved

//       result map { result =>
//         result.head.sourceTimestamp must beSome
//         result.head.sourceTimestamp.get.isEqual(DateTime.parse("2017-04-20T14:37:27+00:00")) must beTrue
//       } await (3, 10.seconds)
//     }
//   }

// }

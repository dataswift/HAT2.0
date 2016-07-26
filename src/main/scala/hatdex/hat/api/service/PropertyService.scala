/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package hatdex.hat.api.service

import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

trait PropertyService extends DataService {

  def getPropertyRelationshipValues(propertyRel: ApiPropertyRelationshipStatic): Future[ApiPropertyRelationshipStatic] = {
    // For each property relationship (should only ever be one)
    val fieldDataRetrieved = (propertyRel.field.id, propertyRel.record.id) match {
      // that has both data field with id and data record with id
      case (Some(propertyFieldId), Some(propertyRecordId)) => getFieldRecordValue(propertyFieldId, propertyRecordId)
      case _ => Future.successful(None)
    }
    fieldDataRetrieved map {
      // Copy the new Data Field with data if found
      case Some(fieldData) => propertyRel.copy(field = fieldData)
      // Otherwise leave as is
      case None            => propertyRel
    }
  }

  def getPropertyRelationshipValues(propertyRel: ApiPropertyRelationshipDynamic): Future[ApiPropertyRelationshipDynamic] = {
    // For each property relationship (should only ever be one)
    val fieldDataRetrieved = propertyRel.field.id match {
      // that has both data field with id and data record with id
      case Some(propertyFieldId) => getFieldValues(propertyFieldId)
      case _                     => Future.successful(None)
    }
    fieldDataRetrieved map {
      // Copy the new Data Field with data if found
      case Some(fieldData) => propertyRel.copy(field = fieldData)
      // Otherwise leave as is
      case None            => propertyRel
    }
  }

  protected def storeProperty(property: ApiProperty): Future[ApiProperty] = {
    (property.propertyType.id, property.unitOfMeasurement.id) match {
      case (Some(typeId: Int), Some(uomId: Int)) =>
        val row = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(),
          property.name, property.description, typeId, uomId)
        DatabaseInfo.db.run {
          ((SystemProperty returning SystemProperty) += row).asTry
        } map {
          case Success(created) => ApiProperty.fromDbModel(created)(property.propertyType, property.unitOfMeasurement)
          case Failure(e)       => throw new RuntimeException("Error while inserting new property")
        }
      case (Some(typeId), None) => Future { throw new IllegalArgumentException("Property must have an existing Type with ID") }
      case (None, _)            => Future { throw new IllegalArgumentException("Property must have an existing Unit of Measurement with ID") }
    }
  }

  protected def getProperty(propertyId: Int): Future[Option[ApiProperty]] = {
    val propertyQuery = for {
      property <- SystemProperty.filter(_.id === propertyId)
      systemType <- property.systemTypeFk
      uom <- property.systemUnitofmeasurementFk
    } yield (property, systemType, uom)

    val property = DatabaseInfo.db.run(propertyQuery.take(1).result)

    property.map(_.headOption map {
      case (property: SystemPropertyRow, systemType: SystemTypeRow, uom: SystemUnitofmeasurementRow) =>
        ApiProperty.fromDbModel(property)(ApiSystemType.fromDbModel(systemType), ApiSystemUnitofmeasurement.fromDbModel(uom))
    })
  }

  protected def getProperties(maybePropertyName: Option[String]): Future[Seq[ApiProperty]] = {
    val propertiesNamed = maybePropertyName match {
      case Some(proeprtyName) => SystemProperty.filter(_.name === proeprtyName)
      case None               => SystemProperty
    }

    val propertiesQuery = for {
      property <- propertiesNamed
      systemType <- property.systemTypeFk
      uom <- property.systemUnitofmeasurementFk
    } yield (property, systemType, uom)

    val fProperties = DatabaseInfo.db.run(propertiesQuery.result)

    fProperties map { properties =>
      properties map {
        case (property: SystemPropertyRow, systemType: SystemTypeRow, uom: SystemUnitofmeasurementRow) =>
          ApiProperty.fromDbModel(property)(ApiSystemType.fromDbModel(systemType), ApiSystemUnitofmeasurement.fromDbModel(uom))
      }
    }
  }
}

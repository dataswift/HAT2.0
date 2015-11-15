package hatdex.hat.api.service

import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.util.{Failure, Try}


trait PropertyService extends DataService {
   def getPropertyRelationshipValues(propertyRel: ApiPropertyRelationshipStatic)
                                    (implicit session: Session): ApiPropertyRelationshipStatic = {
     // For each property relationship (should only ever be one)
     val fieldDataRetrieved = (propertyRel.field.id, propertyRel.record.id) match {
       // that has both data field with id and data record with id
       case (Some(propertyFieldId), Some(propertyRecordId)) =>
         // get the values
         getFieldRecordValue(propertyFieldId, propertyRecordId)
       case _ =>
         None
     }
     fieldDataRetrieved match {
       case Some(fieldData) =>
         // Copy the new Data Field with data if found
         propertyRel.copy(field = fieldData)
       case None =>
         // Otherwise leave as is
         propertyRel
     }
   }

   def getPropertyRelationshipValues(propertyRel: ApiPropertyRelationshipDynamic)
                                    (implicit session: Session): ApiPropertyRelationshipDynamic = {
     // For each property relationship (should only ever be one)
     val fieldDataRetrieved = propertyRel.field.id match {
       // that has both data field with id and data record with id
       case Some(propertyFieldId) =>
         // get the values
         getFieldValues(propertyFieldId)
       case _ =>
         None
     }
     fieldDataRetrieved match {
       case Some(fieldData) =>
         // Copy the new Data Field with data if found
         propertyRel.copy(field = fieldData)
       case None =>
         // Otherwise leave as is
         propertyRel
     }
   }

   protected def storeProperty(property: ApiProperty)(implicit session: Session): Try[ApiProperty] = {
     (property.propertyType.id, property.unitOfMeasurement.id) match {
       case (Some(typeId: Int), Some(uomId: Int)) =>
         val row = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(),
           property.name, property.description,
           typeId, uomId)
         val createdTry = Try((SystemProperty returning SystemProperty) += row)
         createdTry map { created =>
           ApiProperty.fromDbModel(created)(property.propertyType, property.unitOfMeasurement)
         }
       case (Some(typeId), None) =>
         Failure(new IllegalArgumentException("Property must have an existing Type with ID"))
       case (None, _) =>
         Failure(new IllegalArgumentException("Property must have an existing Unit of Measurement with ID"))
     }
   }

   protected def getProperty(propertyId: Int)(implicit session: Session): Option[ApiProperty] = {
     val propertyQuery = for {
       property <- SystemProperty.filter(_.id === propertyId)
       systemType <- property.systemTypeFk
       uom <- property.systemUnitofmeasurementFk
     } yield (property, systemType, uom)

     val property = propertyQuery.run.headOption

     property map {
       case (property: SystemPropertyRow, systemType: SystemTypeRow, uom: SystemUnitofmeasurementRow) =>
         ApiProperty.fromDbModel(property)(ApiSystemType.fromDbModel(systemType), ApiSystemUnitofmeasurement.fromDbModel(uom))
     }
   }

   protected def getProperties(maybePropertyName: Option[String])(implicit session: Session): Seq[ApiProperty] = {
     val propertiesNamed = maybePropertyName match {
       case Some(proeprtyName) =>
         SystemProperty.filter(_.name === proeprtyName)
       case None =>
         SystemProperty
     }

     val propertiesQuery = for {
       property <- propertiesNamed
       systemType <- property.systemTypeFk
       uom <- property.systemUnitofmeasurementFk
     } yield (property, systemType, uom)

     val properties = propertiesQuery.run

     properties map {
       case (property: SystemPropertyRow, systemType: SystemTypeRow, uom: SystemUnitofmeasurementRow) =>
         ApiProperty.fromDbModel(property)(ApiSystemType.fromDbModel(systemType), ApiSystemUnitofmeasurement.fromDbModel(uom))
     }
   }

 }


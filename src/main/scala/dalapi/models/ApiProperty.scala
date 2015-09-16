package dalapi.models

import dal.Tables._
import org.joda.time.LocalDateTime

case class ApiProperty(
                        id: Option[Int],
                        dateCreated: Option[LocalDateTime],
                        lastUpdated: Option[LocalDateTime],
                        name: String,
                        description: Option[String],
                        propertyType: ApiSystemType,
                        unitOfMeasurement: ApiSystemUnitofmeasurement)

object ApiProperty {
  def fromDbModel(property: SystemPropertyRow)
                 (propertyType: ApiSystemType, propertyUom: ApiSystemUnitofmeasurement) : ApiProperty = {
    new ApiProperty(Some(property.id),
      Some(property.dateCreated), Some(property.lastUpdated),
      property.name, property.description,
      propertyType, propertyUom)
  }
}

case class ApiRelationship(relationshipType: String)

case class ApiPropertyRelationshipDynamic(
                                           id: Option[Int],
                                           dateCreated: Option[LocalDateTime],
                                           lastUpdated: Option[LocalDateTime],
                                           relationshipType: String,
                                           field: ApiDataField)

object ApiPropertyRelationshipDynamic {
  def fromDbModel(relationship: EventsSystempropertydynamiccrossrefRow)
                 (property: ApiProperty, field: ApiDataField): ApiPropertyRelationshipDynamic = {
    new ApiPropertyRelationshipDynamic(Some(relationship.id),
      Some(relationship.dateCreated), Some(relationship.lastUpdated),
      relationship.relationshipType, field)
  }

  def fromDbModel(crossref: EventsSystempropertydynamiccrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
                  propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow) : ApiPropertyRelationshipDynamic = {
    val apiUom = ApiSystemUnitofmeasurement.fromDbModel(propertyUom)
    val apiType = ApiSystemType.fromDbModel(propertyType)
    val apiProperty = ApiProperty.fromDbModel(property)(apiType, apiUom)

    val apiDataField = ApiDataField.fromDataField(field)

    fromDbModel(crossref)(apiProperty, apiDataField)
  }

  def fromDbModel(relationship: LocationsSystempropertydynamiccrossrefRow)
                 (property: ApiProperty, field: ApiDataField): ApiPropertyRelationshipDynamic = {
    new ApiPropertyRelationshipDynamic(Some(relationship.id),
      Some(relationship.dateCreated), Some(relationship.lastUpdated),
      relationship.relationshipType, field)
  }

  def fromDbModel(crossref: LocationsSystempropertydynamiccrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
                  propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow) : ApiPropertyRelationshipDynamic = {
    val apiUom = ApiSystemUnitofmeasurement.fromDbModel(propertyUom)
    val apiType = ApiSystemType.fromDbModel(propertyType)
    val apiProperty = ApiProperty.fromDbModel(property)(apiType, apiUom)

    val apiDataField = ApiDataField.fromDataField(field)

    fromDbModel(crossref)(apiProperty, apiDataField)
  }
}

case class ApiPropertyRelationshipStatic(
                                          id: Option[Int],
                                          dateCreated: Option[LocalDateTime],
                                          lastUpdated: Option[LocalDateTime],
                                          relationshipType: String,
                                          field: ApiDataField,
                                          record: ApiDataRecord)

object ApiPropertyRelationshipStatic {
  def fromDbModel(relationship: EventsSystempropertystaticcrossrefRow)
                 (property: ApiProperty, field: ApiDataField, record: ApiDataRecord): ApiPropertyRelationshipStatic = {
    new ApiPropertyRelationshipStatic(Some(relationship.id),
      Some(relationship.dateCreated), Some(relationship.lastUpdated),
      relationship.relationshipType, field, record)
  }

  def fromDbModel(crossref: EventsSystempropertystaticcrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
                  propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow, record: DataRecordRow) : ApiPropertyRelationshipStatic = {
    val apiUom = ApiSystemUnitofmeasurement.fromDbModel(propertyUom)
    val apiType = ApiSystemType.fromDbModel(propertyType)
    val apiProperty = ApiProperty.fromDbModel(property)(apiType, apiUom)

    val apiRecord = ApiDataRecord.fromDataRecord(record)(None)
    val apiDataField = ApiDataField.fromDataField(field)

    fromDbModel(crossref)(apiProperty, apiDataField, apiRecord)
  }

  def fromDbModel(relationship: LocationsSystempropertystaticcrossrefRow)
                 (property: ApiProperty, field: ApiDataField, record: ApiDataRecord): ApiPropertyRelationshipStatic = {
    new ApiPropertyRelationshipStatic(Some(relationship.id),
      Some(relationship.dateCreated), Some(relationship.lastUpdated),
      relationship.relationshipType, field, record)
  }

  def fromDbModel(crossref: LocationsSystempropertystaticcrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
                  propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow, record: DataRecordRow) : ApiPropertyRelationshipStatic = {
    val apiUom = ApiSystemUnitofmeasurement.fromDbModel(propertyUom)
    val apiType = ApiSystemType.fromDbModel(propertyType)
    val apiProperty = ApiProperty.fromDbModel(property)(apiType, apiUom)

    val apiRecord = ApiDataRecord.fromDataRecord(record)(None)
    val apiDataField = ApiDataField.fromDataField(field)

    fromDbModel(crossref)(apiProperty, apiDataField, apiRecord)
  }
}

case class ApiSystemType(
                          id: Option[Int],
                          dateCreated: Option[LocalDateTime],
                          lastUpdated: Option[LocalDateTime],
                          name: String,
                          description: Option[String])

object ApiSystemType {
  def fromDbModel(systemType: SystemTypeRow) = {
    new ApiSystemType(Some(systemType.id),
      Some(systemType.dateCreated), Some(systemType.lastUpdated),
      systemType.name, systemType.description)
  }
}

case class ApiSystemUnitofmeasurement(
                                       id: Option[Int],
                                       dateCreated: Option[LocalDateTime],
                                       lastUpdated: Option[LocalDateTime],
                                       name: String,
                                       description: Option[String],
                                       symbol: Option[String])

object ApiSystemUnitofmeasurement {
  def fromDbModel(uom: SystemUnitofmeasurementRow) = {
    new ApiSystemUnitofmeasurement(Some(uom.id),
      Some(uom.dateCreated), Some(uom.lastUpdated),
      uom.name, uom.description, uom.symbol)
  }
}

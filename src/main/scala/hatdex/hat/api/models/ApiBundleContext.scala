package hatdex.hat.api.models

import org.joda.time.LocalDateTime

case class ApiBundleContextEntitySelection(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    entityName: Option[String],
    entityId: Option[Int],
    entityKind: Option[String],
    properties: Option[Seq[ApiBundleContextPropertySelection]])

case class ApiBundleContextPropertySelection(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    propertyRelationshipKind: Option[String],
    propertyRelationshipId: Option[Int],
    propertyName: Option[String],
    propertyType: Option[String],
    propertyUnitofmeasurement: Option[String])


case class ApiBundleContext(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    name: String,
    entities: Option[Seq[ApiBundleContextEntitySelection]],
    bundles: Option[Seq[ApiBundleContext]])
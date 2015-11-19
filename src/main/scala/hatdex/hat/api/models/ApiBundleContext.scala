package hatdex.hat.api.models

import org.joda.time.LocalDateTime

case class ApiEntitySelection(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    entityname: String,                     // FIXME: inconsistent camelCase    FIXME: entityName should be optional
    table: ApiEntity,                       // FIXME: ApiEntity not defined     FIXME: specific entity should be optional
    entitykind: String)                     // FIXME: inconsistent camelCase    FIXME: entityKind should be optional

object ApiEntitySelection {
  def fromEntitySelection(entitySelection: EntitySelectionRow)(table: ApiEntity) : ApiEntitySelection = {
    new ApiEntitySelection(Some(entitySelection.id),
      Some(entitySelection.dateCreated), Some(entitySelection.lastUpdated),
      entitySelection.name, table, entitykind)
  }
}


case class ApiBundleContext(
    selftable: ApiBundleContext,                // FIXME: selftable? Should be a list of children bundles
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    name: String,
    table: ApiEntitySelection,                  // FIXME: table?
    slices: Option[Seq[ApiBundleTableSlice]])   // FIXME: where do ApiBundleTableSlices come here from? At no point in contextual bundles it is dealt directly with tables

object ApiBundleContext {
  def fromBundleContext(bundleContext: BundleContextRow)(selftable: ApiBundleContext)(table: ApiEntitySelection) : ApiBundleTable = { // FIXME: returning ApiBundleTable when should be creating ApiBundleContext
    new ApiBundleTable(selftable, Some(bundleContext.id),
      Some(bundleContext.dateCreated), Some(bundleContext.lastUpdated),
      bundleContext.name, table, None)
  }
}
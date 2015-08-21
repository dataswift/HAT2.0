package dalapi.models

import dal.Tables._
import org.joda.time.LocalDateTime

object ComparisonOperator extends Enumeration {
  type ComparisonOperator = Value
  val equals, like, likeTime = Value
}

import ComparisonOperator._

/*
  EXAMPLE:
  condition: {
    field: {
      id: 1,
      tableId: 1,
      name: "location"
    },
    value: "home",
    operator: "equals"
  }
 */
case class ApiBundleTableCondition(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    field: ApiDataField,
    value: String,
    operator: ComparisonOperator)

object ApiBundleTableCondition {
  def fromBundleTableSliceCondition(condition: BundleTablesliceconditionRow)(field: ApiDataField) : ApiBundleTableCondition = {
    new ApiBundleTableCondition(Some(condition.id),
      Some(condition.dateCreated), Some(condition.lastUpdated),
      field, condition.value,
      ComparisonOperator.withName(condition.operator))
  }
}

/*
  EXAMPLE:
  slice: {
    table: {
      id: 1
      name: "event",
      source: "facebook",
    },
    conditions: [
      {
        field: {
          id: 1,
          tableId: 1,
          name: "location"
        },
        value: "home",
        operator: "equals"
      },
      {
        field: {
          id: 2,
          tableId: 1,
          name: "startTime"
        },
        value: "saturday",
        operator: "equals"
      },
    ]
  }
 */
case class ApiBundleTableSlice(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    table: ApiDataTable,
    conditions: Seq[ApiBundleTableCondition])

object ApiBundleTableSlice {
  def fromBundleTableSlice(slice: BundleTablesliceRow)(table: ApiDataTable) : ApiBundleTableSlice = {
    new ApiBundleTableSlice(Some(slice.id),
      Some(slice.dateCreated), Some(slice.lastUpdated),
      table, Seq())
  }
}

/*
  EXAMPLE:
  {
    name: "Weekend events at home"
    table: {
      id: 1
      name: "event",
      source: "facebook",
    },
    slices: [
      [
        {
          field: {
            id: 1,
            tableId: 1,
            name: "location"
          },
          value: "home",
          operator: "equals"
        },
        {
          field: {
            id: 2,
            tableId: 1,
            name: "startTime"
          },
          value: "saturday",
          operator: "equals"
        },
      ],
      [
        {
          field: {
            id: 1,
            tableId: 1,
            name: "location"
          },
          value: "home",
          operator: "equals"
        },
        {
          field: {
            id: 2,
            tableId: 1,
            name: "startTime"
          },
          value: "sunday",
          operator: "equals"
        },
      ]
    ]
  }

  EXAMPLE:
  {
    name: "Electricity in the kitchen"
    table: {
      id: 3,
      name: "kichenElectricity",
      source: "fibaro"
    }
  }
 */
case class ApiBundleTable(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    name: String,
    table: ApiDataTable,
    slices: Option[Seq[ApiBundleTableSlice]])

object ApiBundleTable {
  def fromBundleTable(bundleTable: BundleTableRow)(table: ApiDataTable) : ApiBundleTable = {
    new ApiBundleTable(Some(bundleTable.id),
      Some(bundleTable.dateCreated), Some(bundleTable.lastUpdated),
      bundleTable.name, table, None)
  }
}

/*
  EXAMPLE:
  combination1: {
    name: "Weekend events at home",
    bundleTable: {
      id: 1,
      name: "Weekend events at home"
    },
  }

  EXAMPLE:
  combination2: {
    name: "Electricity in the kitchen",
    bundleTable: {
      id: 2,
      name: "Electricity in the kitchen"
    },
    bundleJoinField: {
      id: 2,
      tableId: 1,
      name: "startTime"
    },
    bundleTableField: {
      id: 3,
      tableId: 3,
      name: "timestamp"
    },
    operator: "likeTime"
  }
 */

case class ApiBundleCombination(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    name: String,
    bundleTable: ApiBundleTable,
    bundleJoinField: Option[ApiDataField],
    bundleTableField: Option[ApiDataField],
    operator: Option[ComparisonOperator])


/*
  EXAMPLE:
  bundle: {
    name: "Kitchen electricity on weekend parties",
    tables: [
      combination1,
      combination2
    ]
  }
 */
case class ApiBundleContextless(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    name: String,
    tables: Seq[ApiBundleCombination])
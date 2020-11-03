/*
 * Copyright (c) 2013-2020 kopiLeft Services SARL, Tunis TN
 * Copyright (c) 1990-2020 kopiRight Managed Solutions GmbH, Wien AT
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.kopi.galite.visual

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.kopi.galite.db.DBContextHandler
import org.kopi.galite.db.DBSchema
import org.kopi.galite.util.base.InconsistencyException

object VDatabaseUtils {
  val references = DBSchema.references

  fun checkForeignKeys(context: DBContextHandler, id: Int, table: String) {
    Database.connect(context.getDBContext().defaultConnection.url).useNestedTransactions

    transaction {

      val query1 = references.slice(references.table, references.column, references.action)
              .select { references.reference eq table }
              .orderBy(references.action to SortOrder.DESC)
      val action = query1.forEach { query1Row ->
        val auxTable = object : Table(query1Row[references.table]) {
          var id = integer("ID")
          val column = integer(query1Row[references.column])
        }
        when (query1Row[references.action][0]) {
          'R' -> transaction {
            val query2 = auxTable.slice(auxTable.id)
                    .select { auxTable.column eq id }
            if (query2.toList()[1] != null) {
              throw VExecFailedException(MessageCode.getMessage("VIS-00021", arrayOf<Any>(
                      query1Row[references.column],
                      query1Row[references.table]
              )))
            }
          }

          'C' -> transaction {
            val query2 = auxTable.slice(auxTable.id)
                    .select { auxTable.column eq id }
            query2.forEach {
              checkForeignKeys(context, it[auxTable.id], query1Row[references.table])
            }
            auxTable.deleteWhere { auxTable.column eq id }
          }

          'N' -> transaction {
            auxTable.update({ auxTable.column eq id }) {
              it[auxTable.column] = 0
            }
          }
          else -> throw InconsistencyException()

        }
      }
    }
  }

  fun deleteRecords(context: DBContextHandler, table: String, condition: String?) {
    transaction {
      val auxTable = object : Table(table) {
        var id = integer("ID")
      }
      val query: org.jetbrains.exposed.sql.Query = if (condition != null && condition.isNotEmpty()) {
        auxTable.slice(auxTable.id).select {  auxTable.id  eq condition as Int  }.forUpdate()
      } else {
        auxTable.slice(auxTable.id).selectAll().forUpdate()
      }

      query.forEach {
        checkForeignKeys(context, it[auxTable.id], table)
        Table(table).deleteAll()
      }
    }
  }
}

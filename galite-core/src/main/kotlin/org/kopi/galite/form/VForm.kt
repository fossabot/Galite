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

package org.kopi.galite.form

import org.kopi.galite.visual.VActor
import java.sql.SQLException

import org.kopi.galite.visual.VWindow

abstract class VForm : VWindow() {
  open fun getActiveBlock(): VBlock? {
    TODO()
  }

  open fun setTextOnFieldLeave(): Boolean {
    TODO()
  }

  open fun forceCheckList(): Boolean {
    TODO()
  }

  fun startProtected(message: String?) {
    TODO()
  }

  open fun commitProtected() {
    TODO()
  }

  open fun abortProtected(reason: SQLException) {
    TODO()
  }

  open fun abortProtected(reason: Error) {
    TODO()
  }

  open fun abortProtected(reason: RuntimeException) {
    TODO()
  }

  fun getDefaultActor(type: Int): VActor = TODO()

  fun isChanged(): Boolean = TODO()

  fun getBlock(i: Int): VBlock = TODO()

  companion object {
    const val CMD_NEWITEM = -2
    const val CMD_EDITITEM = -3
    const val CMD_EDITITEM_S = -4
    const val CMD_AUTOFILL = -5
  }
}

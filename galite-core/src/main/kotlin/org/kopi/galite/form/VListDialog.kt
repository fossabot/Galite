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

import org.kopi.galite.list.VListColumn
import org.kopi.galite.visual.VWindow

class VListDialog {

  constructor(arrayOf: Array<VListColumn>?, arrayOf1: Array<Array<Any?>>?) {
    TODO()
  }

  constructor(list: Array<VListColumn>?,
              data: Array<Array<Any?>>?,
              rows: Int,
              newForm: VDictionary?) {
    TODO()
  }

  fun selectFromDialog(form: VForm, field: VField?): Int = TODO()
  fun selectFromDialog(form: VForm?, window: VWindow?, field: VField?): Int {
   TODO()
  }
}

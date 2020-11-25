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

open class VCommand(private var mode: Int,
                    protected var handler: ActionHandler?,
                    var actor: VActor?,
                    internal val trigger: Int,
                    internal val action: (() -> Unit)?,
                    val item: String) {

  /**
   * Kill a command: this command will never been enabled again
   */
  fun kill() {
    killed = true
  }

  init {
    println(" test -----------VCOMM!!---------------")
            val x = null
    print(x)
    /*throw (IllegalCallerException())*/
  }

  /**
   * Returns the actor
   */
  open fun setEnabled(enabled: Boolean) {
    this.actor?.let {
      if (!killed) {
        it.isEnabled = enabled
        it.number = trigger
        it.action = action
        it.handler = handler
      }
    }
  }

  /**
   * Returns true iff the command is active in given to mode.
   *
   * @param    mode        the mode to test
   */
  fun isActive(mode: Int): Boolean = this.mode and (1 shl mode) != 0

  /**
   * Returns the actor
   */
  fun isEnabled(): Boolean = actor != null && actor!!.isEnabled

  /**
   * Returns the name has defined in source
   */
  fun getIdent(): String = item

  /**
   * Returns true iff the command is active in given to mode.
   *
   * @param    b            set to be active
   */
  fun setActive(b: Boolean) {
    mode = if (b) {
      0xFFFF
    } else {
      0
    }
  }

  fun performAction() {
    with(actor!!) {
      number = trigger
      handler = handler
      performAction()
    }
  }

  fun performBasicAction() {
    with(actor!!) {
      number = trigger
      handler = handler
      performBasicAction()
    }
  }

  /**
   * Returns the actor
   */
  open fun getKey(): Int {
    return actor?.acceleratorKey ?: 0
  }

  // ----------------------------------------------------------------------
  // HELP HANDLING
  // ----------------------------------------------------------------------
  open fun helpOnCommand(help: VHelpGenerator) {
    actor!!.helpOnCommand(help)
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------
  private var killed = false
}

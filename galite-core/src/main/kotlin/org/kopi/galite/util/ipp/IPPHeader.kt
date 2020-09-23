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
package org.kopi.galite.util.ipp

class IPPHeader(var majorVersion: Byte = 1,
                var minorVersion: Byte = 1,
                var operationID: Short = 0,
                var requestID: Int = 0) {
  // --------------------------------------------------------------------
  // CONSTRUCTOR
  // --------------------------------------------------------------------
  constructor(inputStream: IPPInputStream) {
    majorVersion = inputStream.readByte()
    minorVersion = inputStream.readByte()
    operationID = inputStream.readShort()
    requestID = inputStream.readInteger()
  }

  // --------------------------------------------------------------------
  // ACCESSORS
  // --------------------------------------------------------------------
  fun setVersion(major: Byte, minor: Byte) {
    majorVersion = major
    minorVersion = minor
  }

  fun write(os: IPPOutputStream) {
    os.writeByte(majorVersion.toInt())
    os.writeByte(minorVersion.toInt())
    os.writeShort(operationID.toInt())
    os.writeInteger(requestID)
  }

  val size: Int
    get() = 8

  fun dump() {
    println("Major version : $majorVersion")
    println("Minor version : $minorVersion")
    println("Operation ID : $operationID")
    println("Request ID : $requestID")
  }

  val isAnError: Boolean
    get() = operationID >= 0x400

  fun getStatus(): String? {
    val units: Int
    when {
      operationID < 0x400 -> {
        units = operationID.toInt()
        if (units < IPPConstants.ERR_SUCCESSFUL.size) {
          return IPPConstants.ERR_SUCCESSFUL[units]
        }
      }
      operationID < 0x500 -> {
        units = operationID - 0x400
        if (units < IPPConstants.ERR_CLIENT_ERROR.size) {
          return IPPConstants.ERR_CLIENT_ERROR[units]
        }
      }
      operationID < 0x500 -> {
        units = operationID - 0x400
        if (units < IPPConstants.ERR_CLIENT_ERROR.size) {
          return IPPConstants.ERR_CLIENT_ERROR[units]
        }
      }
      else -> {
        units = operationID - 0x400
        if (units < IPPConstants.ERR_SERVER_ERROR.size) {
          return IPPConstants.ERR_SERVER_ERROR[units]
        }
      }
    }
    return null
  }
}

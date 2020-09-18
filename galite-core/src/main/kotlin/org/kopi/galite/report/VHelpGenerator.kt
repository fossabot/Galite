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

package org.kopi.galite.report

import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.FileOutputStream
import java.io.IOException

import org.kopi.galite.base.Utils
import org.kopi.galite.visual.VCommand
import org.kopi.galite.visual.VHelpGenerator

/**
 * This class implements a Kopi pretty printer
 */
class VHelpGenerator : VHelpGenerator() {
  // ----------------------------------------------------------------------
  // ACCESSORS
  // ----------------------------------------------------------------------
  /**
   * prints a compilation unit
   */
  fun helpOnReport(name: String,
                   commands: Array<VCommand>,
                   model: MReport,
                   help: String): String? {
    return try {
      val file: File
      val version: Array<String>
      file = Utils.getTempFile(name.replace("[:\\\\/*\"?|<>']".toRegex(), " "), "htm")
      val p = PrintWriter(BufferedWriter(OutputStreamWriter(FileOutputStream(file), "UTF-8")))
      p.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//DE\">")
      p.println("<!--Generated by kopi help generator-->")
      p.println("<TITLE>$name</TITLE>")
      p.println("<META NAME=\"description\" CONTENT=\"$name\">")
      p.println("<META NAME=\"keywords\" CONTENT=\"$name\">")
      p.println("<META NAME=\"resource-type\" CONTENT=\"document\">")
      p.println("<META NAME=\"distribution\" CONTENT=\"global\">")
      p.println("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=utf-8\">")
      p.println("</HEAD>")
      p.println("<BODY BGCOLOR=#FFFFFF>")
      p.println("<CENTER><H1>$name</H1></CENTER>")
      if (help != null) {
        p.println("<P>$help</P>")
      }
      commands?.let { helpOnCommands(it) }
      val columnCount: Int = model.getModelColumnCount()
      p.println("<TABLE border=\"0\" cellspacing=\"3\" cellpadding=\"2\">")
      p.println("<TR>")
      p.println("<TD><pre>    </pre>")
      p.println("</TD>")
      p.println("<TD>")
      p.println("<DL>")
      for (i in 0 until columnCount) {
        val column: VReportColumn = model.getModelColumn(i)
        if (column.options and Constants.CLO_HIDDEN === 0) {
          column.helpOnColumn(this)
        }
      }
      p.println("</DL>")
      p.println("</TD>")
      p.println("</TR>")
      p.println("</TABLE>")
      p.println("<BR>")
      p.println("<ADDRESS>")
      p.println("<I>kopiRight Managed Solutions GmbH</I><BR>")
      version = Utils.getVersion()
      for (i in version.indices) {
        p.println("<I>" + version[i] + "</I><BR>")
      }
      p.println("</ADDRESS>")
      p.println("</BODY>")
      p.println("</HTML>")
      p.close()
      file.path
    } catch (e: IOException) {
      System.err.println("IO ERROR $e")
      null
    }
  }

  /**
   * printlns a compilation unit
   */
  fun helpOnColumn(label: String?,
                   help: String?) {
    if (label == null) {
      return
    }
    p.println("<DT>")
    p.println("<H2>$label</H2>")
    p.println("<DD>")
    if (help != null) {
      p.println("<P>$help</P>")
    }
  }

  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------
  protected override lateinit var p: PrintWriter
}

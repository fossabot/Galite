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
 * This class implements a pretty printer
 */
class VHelpGenerator : VHelpGenerator() {
  // ----------------------------------------------------------------------
  // ACCESSORS
  // ----------------------------------------------------------------------
  /**
   * prints a compilation unit
   */
  fun helpOnReport(name: String,
                   commands: Array<VCommand>?,
                   model: MReport,
                   help: String?): String? {
    return try {
      val file: File = Utils.getTempFile(name.replace("[:\\\\/*\"?|<>']".toRegex(), " "), "htm")
      print = PrintWriter(BufferedWriter(OutputStreamWriter(FileOutputStream(file), "UTF-8")))
      print.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//DE\">")
      print.println("<!--Generated by the help generator-->")
      print.println("<TITLE>$name</TITLE>")
      print.println("<META NAME=\"description\" CONTENT=\"$name\">")
      print.println("<META NAME=\"keywords\" CONTENT=\"$name\">")
      print.println("<META NAME=\"resource-type\" CONTENT=\"document\">")
      print.println("<META NAME=\"distribution\" CONTENT=\"global\">")
      print.println("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=utf-8\">")
      print.println("</HEAD>")
      print.println("<BODY BGCOLOR=#FFFFFF>")
      print.println("<CENTER><H1>$name</H1></CENTER>")
      if (help != null) {
        print.println("<P>$help</P>")
      }
      helpOnCommands(commands!!)
      val columnCount: Int = model.getModelColumnCount()
      print.println("<TABLE border=\"0\" cellspacing=\"3\" cellpadding=\"2\">")
      print.println("<TR>")
      print.println("<TD><pre>    </pre>")
      print.println("</TD>")
      print.println("<TD>")
      print.println("<DL>")
      for (i in 0 until columnCount) {
        val column: VReportColumn = model.getModelColumn(i)
        if (column.options and Constants.CLO_HIDDEN == 0) {
          column.helpOnColumn(this)
        }
      }
      print.println("</DL>")
      print.println("</TD>")
      print.println("</TR>")
      print.println("</TABLE>")
      print.println("<BR>")
      print.println("<ADDRESS>")
      print.println("<I>kopiLeft Services SARL, Tunis TN</I><BR>")
      print.println("<I>kopiRight Managed Solutions GmbH, Wien AT</I><BR>")
      val version = Utils.getVersion()
      for (i in version.indices) {
        print.println("<I>" + version[i] + "</I><BR>")
      }
      print.println("</ADDRESS>")
      print.println("</BODY>")
      print.println("</HTML>")
      print.close()
      file.path
    } catch (e: IOException) {
      System.err.println("IO ERROR $e")
      null
    }
  }

  /**
   * prints a compilation unit
   */
  fun helpOnColumn(label: String?,
                   help: String?) {
    if (label == null) {
      return
    }
    print.println("<DT>")
    print.println("<H2>$label</H2>")
    print.println("<DD>")
    if (help != null) {
      print.println("<P>$help</P>")
    }
  }

  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------
  protected override lateinit var print: PrintWriter
}

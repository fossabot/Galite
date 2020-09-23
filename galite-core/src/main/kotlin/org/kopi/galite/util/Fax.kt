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
package org.kopi.galite.util

import org.kopi.galite.base.Utils

import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ConnectException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.StringTokenizer
import java.util.Vector
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

class Fax(port: Int, host: String) {
  fun login(uname: String): Int {
    Utils.log("Fax", "login:$uname")
    print("USER $uname\n")
    clntOut.flush()
    val answer = check(readLine())
    if (answer == NEEDS_PASSWD) {
      print("""
  PASS 0
  
  """.trimIndent())
      clntOut.flush()
    }
    return answer
  }


  fun endCon() {
    print("""
  QUIT
  
  """.trimIndent())
    clntOut.flush()
    check(readLine())
    clntIn.close()
    clntOut.close()
    clnt.close()
  }

  fun sendBuffer(inputStream: InputStream): String {
    val sndsrv: SendServ
    val pstr: String
    val df = Deflater(9, false)
    val baos = ByteArrayOutputStream()
    val dos = DeflaterOutputStream(baos, df, inputStream.available())
    val buffer = ByteArray(1024)
    var read: Int
    while (inputStream.read(buffer, 0, 1024).also { read = it } != -1) {
      dos.write(buffer, 0, read)
    }
    dos.close()

    sndsrv = SendServ(baos.toByteArray(), debug)
    val iaddr: ByteArray = getInetAddr()
    pstr = makePORT(iaddr, sndsrv.port)
    print("""
  TYPE I
  
  """.trimIndent()) // Binaer
    clntOut.flush()
    check(readLine())
    print("""
  MODE Z
  
  """.trimIndent()) // ZIP
    //print("MODE S" + "\n"); // Stream
    clntOut.flush()
    check(readLine())
    print("PORT $pstr\n")
    System.err.println("PORT $pstr\n")
    clntOut.flush()
    check(readLine())
    print("""
  STOT
  
  """.trimIndent())
    clntOut.flush()
    val line = readLine()
    check(line)

    try {
      sndsrv.join()
    } catch (e: InterruptedException) {
    }
    val st = StringTokenizer(line, " ")
    st.nextToken()
    st.nextToken()
    val filename = st.nextToken()
    check(readLine())

    return filename
  }

  fun getReceived(name: String): ByteArray {
    val pstr: String
    val recsrv: RecvServ = RecvServ(debug)
    val iaddr: ByteArray = getInetAddr()
    pstr = makePORT(iaddr, recsrv.port)
    if (debug) {
      println("Fax.getReceived: $pstr")
    }
    print("""
  TYPE I
  
  """.trimIndent())
    clntOut.flush()
    check(readLine())
    print("""
  MODE S
  
  """.trimIndent())
    //print("MODE Z" + "\n");
    clntOut.flush()
    check(readLine())
    print("""
  CWD recvq
  
  """.trimIndent())
    clntOut.flush()
    check(readLine())
    print("PORT $pstr\n")
    clntOut.flush()
    check(readLine())
    print("RETR $name\n")
    clntOut.flush()
    check(readLine())
    check(readLine())

    try {
      recsrv.join()
    } catch (e: InterruptedException) {
    }
    print("""
  CWD
  
  """.trimIndent())
    clntOut.flush()
    check(readLine())
    return recsrv.data
  }

  fun infoS(what: String): String {
    val pstr: String
    val iaddr: ByteArray
    val recsrv: RecvServ

    // Thread erzeugen
    recsrv = RecvServ(debug)
    iaddr = getInetAddr()
    pstr = makePORT(iaddr, recsrv.port)
    print("PORT $pstr\n")
    clntOut.flush()
    check(readLine())
    print("LIST $what\n")
    clntOut.flush()

    try {
      recsrv.join()
    } catch (e: InterruptedException) {
    }
    if (typeOfThread != NONE) {
      throw PROTOException(errText, LOST_CONNECTION)
    }
    if (check(readLine()) == ABOUT_TO_OPEN_DATACON) {
      check(readLine())
    } else {
      throw PROTOException("Fax.infoS: No Data from Faxserver", 1)
    }
    val cnt = recsrv.data.indexOfFirst {
      it.toInt() == 0
    }.takeUnless { it == -1 } ?: recsrv.data.size

    //hibyte - the top 8 bits of each 16-bit Unicode character
    return String(recsrv.data, 0, cnt)
  }

  fun command(what: String): String {
    val response = StringBuffer()
    var line: String
    print("""
  $what
  
  """.trimIndent())
    clntOut.flush()
    val erg = check(readLine())

    if (erg == SYSTEM_STATUS || erg == HELP_MESSAGE) {
      while (true) {
        line = readLine()
        if (check(line) == erg) {
          break
        }
        response.append("""
  $line
  
  """.trimIndent())
      }
    }
    return response.toString()
  }

  private fun setNewJob(jobNumber: String, jobUser: String, id: String) {
    val number = checkNumber(jobNumber)
    val user = DEFAULT_USER
    Utils.log("Fax", "NEW JOB:$id / user: $user")

    // Jobparameter einstellen
    command("JNEW")
    command("JPARM FROMUSER \"$user\"")
    command("JPARM LASTTIME 145959")
    command("JPARM MAXDIALS 3")
    command("JPARM MAXTRIES 3")
    command("JPARM SCHEDPRI 127")
    command("JPARM DIALSTRING \"$number\"")
    command("JPARM NOTIFYADDR \"$user\"")
    command("JPARM JOBINFO \"$id\"")
    command("JPARM VRES 196")
    command("JPARM PAGEWIDTH " + 209)
    command("JPARM PAGELENGTH " + 296)
    command("JPARM NOTIFY \"NONE\"") //1:mail when done
    command("JPARM PAGECHOP \"default\"")
    command("JPARM CHOPTHRESHOLD 3")
  }

  private fun checkNumber(number: String): String {
    return buildString {
      number.filter { it in '0'..'9' }.forEach { append(it) }
    }
  }

  private fun makePORT(iaddr: ByteArray, port: Int): String {
    val a = (port and 0xff).toByte()
    val b = (port and 0xff00 shr 8).toByte()
    return ((0xff and iaddr[0].toInt()).toString() + "," + (0xff and iaddr[1].toInt()) + "," +
            (0xff and iaddr[2].toInt()) + "," + (0xff and iaddr[3].toInt()) + "," +
            (0xff and b.toInt()) + "," + (0xff and a.toInt())).toString()
  }

  private fun print(s: String) {
    if (verboseMode) {
      System.err.print("->$s")
    }
    clntOut.print(s)
  }

  private fun readLine(): String = clntIn.readLine()

  private fun check(str: String?): Int {
    val message = StringBuffer()
    val rtc: Int
    if (str == null) {
      throw PROTOException("Fax.check: empty Reply String!!!",
              EMPTY_REPLY_STRING)
    }
    val delim: String = if (str[3] == '-') {
      "-"
    } else {
      " "
    }
    val st = StringTokenizer(str, delim)

    rtc = try {
      st.nextToken().toInt()
    } catch (e: NumberFormatException) {
      0
    }
    for (i in st.countTokens() downTo 1) {
      message.append(st.nextToken() + " ")
    }
    return when (rtc) {
      SERVICE_NOT_AVAILABLE, NO_DATA_CONNECTION, CONNECTION_CLOSED, FILE_ACTION_NOT_TAKEN, ACTION_ABORTED_ERROR, ACTION_NOT_TAKEN_SPACE, SYNTAX_ERROR_COMMAND, SYNTAX_ERROR_PARAMETER, COMMAND_NOT_IMPLEMENTED, BAD_COMMAND_SEQUENCE, OPERATION_NOT_PERMITTET, NOT_LOGGED_IN, NEED_ACC_FOR_STORING, ACTION_NOT_TAKEN, ACTION_ABORTED_PAGETYPE, FILE_ACTION_ABORTED, FAILED_TO_KILL_JOB, ACTION_NOT_TAKEN_NAME -> throw PROTOException(message.toString(), rtc)
      else -> rtc
    }
  }

  private fun getInetAddr(): ByteArray {
    return if (host == "localhost") {

      byteArrayOf(127, 0, 0, 1)
    } else {
      InetAddress.getLocalHost().address
    }
  }

  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------
  private val debug = false
  private val clnt: Socket
  private val clntIn: BufferedReader
  private val clntOut: PrintWriter
  private val host: String
  // ----------------------------------------------------------------------
  // INNER CLASSES
  // ----------------------------------------------------------------------

  class PROTOException(s: String, val number: Int) : FaxException(s) {

    override val message: String = super.message.toString() + " Replay Code: " + number
  }

  /**
   * Mother class the send and receive thread workers.
   */
  private abstract class BasicServ protected constructor(private val debug1: Boolean) : Thread() {
    protected fun debug(message: String?) {
      if (debug1) {
        println(message)
      }
    }

    val port: Int
    protected val srv: ServerSocket?

    companion object {
      // ----------------------------------------------------------------------
      // DATA CONSTANTS
      // ----------------------------------------------------------------------
      private const val TIMEOUT = 20 // in seconds
    }

    init {
      srv = ServerSocket(0, Companion.TIMEOUT)
      // get next free port
      port = srv.localPort
      debug("BasicServ: port=$port")
      start()
    }
  }

  /**
   * Die Klasse RecServ ist abgeleitet von der Klasse BasicServ.
   * Sie empfaengt Daten vom Protokoll Server
   */
  private class RecvServ(debug: Boolean) : BasicServ(debug) {
    // thread body
    override fun run() {
      val dataInputStream: DataInputStream
      val buf = ByteArray(1024)
      try {
        debug("RecvServ.run: Baue Verbindung auf")
        val srv_clnt = srv!!.accept()
        debug("RecvServ.run: Erzeuge InputStream")
        dataInputStream = DataInputStream(srv_clnt.getInputStream())
        debug("RecvServ.run: Warte auf Daten")
        val out = ByteArrayOutputStream()
        var anz: Int
        while (dataInputStream.read(buf).also { anz = it } > 0) {
          out.write(buf, 0, anz)
        }
        data = out.toByteArray()
        srv.close()
      } catch (e: IOException) {
        fail("RecvServ", e, RECEIVE)
      }
      debug("RecvServ.run: Thread completed!")
    }

    // ----------------------------------------------------------------------
    // DATA MEMBERS
    // ----------------------------------------------------------------------
    lateinit var data: ByteArray
  }

  private class SendServ(private val buf: ByteArray, debug: Boolean) : BasicServ(debug) {
    // thread body
    override fun run() {
      val out: DataOutputStream
      try {
        debug("SendServ.run: \n" + "Build connection")
        val srv_clnt = srv!!.accept()
        debug("SendServ.run: Create OutputStream")
        out = DataOutputStream(srv_clnt.getOutputStream())
        out.write(buf, 0, buf.size)
        out.flush()
        srv_clnt.close()
        debug("SendServ.run: Sent Bytes=" + out.size())
        srv.close()
      } catch (e: IOException) {
        fail("Thread error", e, SEND)
      }
      debug("SendServ.run: Thread Completed!")
    }

  }

  companion object {
    // ----------------------------------------------------------------------
    // CONVENIENCE METHODS TO SEND FAX, GET QUEUE STATUS, ...
    // ----------------------------------------------------------------------
    /**
     * Sends a fax
     */
    fun fax(host: String,
            input: InputStream,
            user: String,
            number: String,
            jobId: String) {
      fax(HFAX_PORT, host, input, user, number, jobId)
    }

    /**
     * Sends a fax
     */
    fun fax(port: Int,
            host: String,
            inputStream: InputStream,
            user: String,
            number: String,
            jobId: String) {
      val fax = Fax(port, host)
      val filename: String
      fax.login(user)
      fax.command("IDLE 900")
      fax.command("TZONE LOCAL")
      filename = fax.sendBuffer(inputStream)
      fax.setNewJob(number, user, jobId)
      fax.command("JPARM DOCUMENT $filename")
      fax.command("JSUBM")
      fax.endCon()
    }

    /*
   * ----------------------------------------------------------------------
   * READ THE SEND QUEUE
   * RETURNS A VECTOR OF STRINGS
   * ----------------------------------------------------------------------
   */
    fun readSendQueue(host: String, user: String): Vector<FaxStatus> = readQueue(host, user, "sendq")

    /*
   * ----------------------------------------------------------------------
   * READ THE DONE QUEUE
   * RETURNS A VECTOR OF FAXSTATUS
   * ----------------------------------------------------------------------
   */
    fun readDoneQueue(host: String, user: String): Vector<FaxStatus> = readQueue(host, user, "doneq")

    /*
   * ----------------------------------------------------------------------
   * READ THE RECEIVE QUEUE
   * RETURNS A VECTOR OF FAXSTATUS
   * ----------------------------------------------------------------------
   */
    fun readRecQueue(host: String, user: String): Vector<FaxStatus> = readQueue(host, user, "recvq")

    /*
   * ----------------------------------------------------------------------
   * HANDLE THE SERVER AND MODEM STATE
   * ----------------------------------------------------------------------
   */
    fun readServerState(host: String, user: String): Vector<String> {
      val queue = Vector<String>()
      try {
        val ret = getQueue(HFAX_PORT, host, user, "status")
        val token = StringTokenizer(ret, "\n")
        Utils.log("Fax", "READ STATE : host $host / user $user")
        while (token.hasMoreElements()) {
          queue.addElement(token.nextElement().toString())
        }
      } catch (e: ConnectException) {
        throw FaxException("NO FAX SERVER")
      } catch (e: Exception) {
        throw FaxException("Trying read server state: " + e.message, e)
      }
      return queue
    }

    /*
   * ----------------------------------------------------------------------
   * HANDLE THE SERVER AND MODEM STATE
   * ----------------------------------------------------------------------
   */
    fun readSendTime(jobId: String): String? = null

    /**
     * Convenience method
     */
    fun killJob(host: String, user: String, job: String) {
      killJob(HFAX_PORT, host, user, job)
    }

    /**
     * Convenience method
     */
    fun killJob(port: Int,
                host: String,
                user: String,
                job: String) {
      val fax = Fax(port, host)
      fax.login(DEFAULT_USER)
      fax.command("JKILL $job")
      Utils.log("Fax", "Kill 1: $job")
      fax.endCon()
    }

    /**
     * Convenience method
     */
    fun clearJob(host: String,
                 user: String,
                 job: String) {
      clearJob(HFAX_PORT, host, user, job)
    }

    /**
     * Convenience method
     */
    fun clearJob(port: Int,
                 host: String,
                 user: String,
                 job: String) {
      val fax = Fax(port, host)
      fax.login(DEFAULT_USER)
      fax.command("JDELE $job")
      Utils.log("Fax", "Delete 1: $job")
      fax.endCon()
    }

    /*
   * ----------------------------------------------------------------------
   * HANDLE THE QUEUES --- ALL QUEUES ARE HANDLED BY THAT METHOD
   * ----------------------------------------------------------------------
   */
    private fun getQueue(port: Int, host: String, user: String, qname: String): String {
      val fax = Fax(port, host)
      val ret: String
      fax.login(user)
      fax.command("IDLE 900")
      fax.command("TZONE LOCAL")
      fax.command("JOBFMT \" %j| %J| %o| %e| %a| %P| %D| %s\"")
      fax.command("RCVFMT \" %f| %t| %s| %p| %h| %e\"")
      fax.command("MDMFMT \"Modem %m (%n): %s\"")
      ret = fax.infoS(qname)
      fax.endCon()
      return ret
    }

    // ----------------------------------------------------------------------
    // IMPLEMENTATION
    // ----------------------------------------------------------------------
    /*
   * ----------------------------------------------------------------------
   * READS ANY QUEUE
   * RETURNS A VECTOR OF STRINGS
   * ----------------------------------------------------------------------
   */
    private fun readQueue(host: String, user: String, qname: String): Vector<FaxStatus> {
      val queue: Vector<FaxStatus> = Vector<FaxStatus>()
      try {
        val ret = getQueue(HFAX_PORT, host, user, qname)
        val token = StringTokenizer(ret, "\n")
        Utils.log("Fax", "READ $qname : host $host / user $user")
        while (token.hasMoreElements()) {
          try {
            val str = token.nextElement().toString()
            val process = StringTokenizer(str, "|")
            if (qname != "recvq") {
              queue.addElement(FaxStatus(process.nextToken().trim { it <= ' ' },  // ID
                      process.nextToken().trim { it <= ' ' },  // TAG
                      process.nextToken().trim { it <= ' ' },  // USER
                      process.nextToken().trim { it <= ' ' },  // DIALNO
                      process.nextToken().trim { it <= ' ' },  // STATE OF CODE
                      process.nextToken().trim { it <= ' ' },  // PAGES
                      process.nextToken().trim { it <= ' ' },  // DIALS
                      process.nextToken().trim { it <= ' ' })) // STATE OF TEXT
            } else {
              queue.addElement(FaxStatus(process.nextToken().trim { it <= ' ' },  // FILENAME %f
                      process.nextToken().trim { it <= ' ' },  // TIME IN %t
                      process.nextToken().trim { it <= ' ' },  // SENDER %s
                      process.nextToken().trim { it <= ' ' },  // PAGES %p
                      process.nextToken().trim { it <= ' ' },  // DURATION %h
                      process.nextToken().trim { it <= ' ' }, "", "")) // TEXT ERROR %e
            }
          } catch (e: Exception) {
            throw FaxException(e.message!!, e)
          }
        }
      } catch (e: ConnectException) {
        Utils.log("Fax", "NO FAX SERVER")
        throw FaxException("NO FAX SERVER")
      } catch (e: Exception) {
        throw FaxException(e.message!!, e)
      }
      return queue
    }

    fun fail(msg: String, e: Exception, which: Int) {
      System.err.println("$msg: $e")
      typeOfThread = which
      errText = "$msg: $e"
    }

    // ----------------------------------------------------------------------
    // DATA CONSTANTS
    // ----------------------------------------------------------------------
    const val ABOUT_TO_OPEN_DATACON = 150
    const val SYSTEM_STATUS = 211
    const val HELP_MESSAGE = 214
    const val NEEDS_PASSWD = 331
    const val SERVICE_NOT_AVAILABLE = 421
    const val NO_DATA_CONNECTION = 425
    const val CONNECTION_CLOSED = 426
    const val FILE_ACTION_NOT_TAKEN = 450
    const val ACTION_ABORTED_ERROR = 451
    const val ACTION_NOT_TAKEN_SPACE = 452
    const val FAILED_TO_KILL_JOB = 460
    const val SYNTAX_ERROR_COMMAND = 500
    const val SYNTAX_ERROR_PARAMETER = 501
    const val COMMAND_NOT_IMPLEMENTED = 502
    const val BAD_COMMAND_SEQUENCE = 503
    const val OPERATION_NOT_PERMITTET = 504
    const val NOT_LOGGED_IN = 530
    const val NEED_ACC_FOR_STORING = 532
    const val ACTION_NOT_TAKEN = 550
    const val ACTION_ABORTED_PAGETYPE = 551
    const val FILE_ACTION_ABORTED = 552
    const val ACTION_NOT_TAKEN_NAME = 553

    const val LOST_CONNECTION = -1
    const val EMPTY_REPLY_STRING = -2
    const val HFAX_PORT = 4559
    const val HFAX_HOST = "localhost"
    const val DEFAULT_USER = "GALITE"
    const val NONE = 0
    const val RECEIVE = 1
    const val SEND = 2
    var typeOfThread = NONE
    const val verboseMode = false
    var errText = ""
  }

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------
  init {
    var port = port
    var host = host
    if (port == 0) {
      port = HFAX_PORT
    }
    this.host = host

    clnt = Socket(host, port)
    clntIn = BufferedReader(InputStreamReader(clnt.getInputStream()))
    clntOut = PrintWriter(clnt.getOutputStream())
    check(readLine())
  }
}

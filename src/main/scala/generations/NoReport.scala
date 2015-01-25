/*
 * Copyright (C) 2015 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package generations

import java.io.{ Writer, PrintWriter }

trait NoReport <: Model {

  def emptyWriter =
    new PrintWriter(new Writer() {
      override def flush(): Unit = {}
      override def write(chars: Array[Char], i: Int, i1: Int): Unit = {}
      override def close(): Unit = {}
    })

  override def writeLineToReportFile(line: String): Unit = {}
  override def writeLineToPlaintextReportFile(line: String): Unit = {}
  override def writeLineToReportFile(line: String, rFile: PrintWriter): Unit = {}
  override def writeBufferToReportFile(line: String, rFile: PrintWriter): Unit = {}
  override def startPlainTextReportFile(baseName: String): PrintWriter = emptyWriter
  override def startReportFile(baseName: String): PrintWriter = emptyWriter
  override def writeParametersToReportFile(): Unit = {}
  override def endReportFile(): Unit = {}
  override def writeChangeToReportFile(varname: String, value: String): Unit = {}
  override def startReportFile(): PrintWriter = emptyWriter
  override def endReportFile(rFile: PrintWriter): Unit = {}
  override def endPlainTextReportFile(rFile: PrintWriter): Unit = {}
  override def writeParametersToReportFile(rFile: PrintWriter): Unit = {}
  override def writeParametersToPlainTextReportFile(rFile: PrintWriter): Unit = {}
  override def getReportFile: PrintWriter = emptyWriter
  override def getPlaintextReportFile: PrintWriter = emptyWriter
}

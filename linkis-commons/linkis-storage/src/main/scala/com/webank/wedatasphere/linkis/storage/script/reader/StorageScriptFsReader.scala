/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.storage.script.reader

import java.io._

import com.webank.wedatasphere.linkis.common.io.{FsPath, MetaData, Record}
import com.webank.wedatasphere.linkis.storage.script._
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils
import org.apache.commons.io.IOUtils

import scala.collection.mutable.ArrayBuffer



class StorageScriptFsReader(val path: FsPath, val charset: String, val inputStream: InputStream) extends ScriptFsReader {

  private var inputStreamReader: InputStreamReader = _
  private var bufferedReader: BufferedReader = _

  private var metadata: ScriptMetaData = _

  private var variables: ArrayBuffer[Variable] = new ArrayBuffer[Variable]()
  private var lineText: String = _

  @scala.throws[IOException]
  override def getRecord: Record = {

    if (metadata == null) throw new IOException("Must read metadata first(必须先读取metadata)")
    val record = new ScriptRecord(lineText)
    lineText = bufferedReader.readLine()
    record
  }

  @scala.throws[IOException]
  override def getMetaData: MetaData = {
    if (metadata == null) init()
    val parser = ParserFactory.listParsers().filter(p => p.belongTo(StorageUtils.pathToSuffix(path.getPath)))
    lineText = bufferedReader.readLine()
    while (hasNext && parser.length > 0 && isMetadata(lineText, parser(0).prefix, parser(0).prefixConf)) {
      variables += parser(0).parse(lineText)
      lineText = bufferedReader.readLine()
    }
    metadata = new ScriptMetaData(variables.toArray)
    metadata
  }

  def init(): Unit = {
    inputStreamReader = new InputStreamReader(inputStream)
    bufferedReader = new BufferedReader(inputStreamReader)
  }

  @scala.throws[IOException]
  override def skip(recordNum: Int): Int = -1

  @scala.throws[IOException]
  override def getPosition: Long = -1L

  @scala.throws[IOException]
  override def hasNext: Boolean = lineText != null

  @scala.throws[IOException]
  override def available: Long = if (inputStream != null) inputStream.available() else 0L

  override def close(): Unit = {
    IOUtils.closeQuietly(bufferedReader)
    IOUtils.closeQuietly(inputStreamReader)
    IOUtils.closeQuietly(inputStream)
  }

  /**
    * Determine if the read line is metadata(判断读的行是否是metadata)
    *
    * @param line
    * @return
    */
  def isMetadata(line: String, prefix: String, prefixConf: String): Boolean = {
    val regex = ("\\s*" + prefix + "\\s*(.+)\\s*" + "=" + "\\s*(.+)\\s*").r
    line match {
      case regex(_, _) => true
      case _ => {
        val split: Array[String] = line.split("=")
        if (split.size != 2) return false
        if (split(0).split(" ").filter(_ != "").size != 4) return false
        if (!split(0).split(" ").filter(_ != "")(0).equals(prefixConf)) return false
        true
      }
    }
  }
}





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

package com.webank.wedatasphere.linkis.storage.script.writer

import java.io.{ByteArrayInputStream, IOException, InputStream, OutputStream}
import java.util

import com.webank.wedatasphere.linkis.common.io.{FsPath, MetaData, Record}
import com.webank.wedatasphere.linkis.storage.LineRecord
import com.webank.wedatasphere.linkis.storage.script.{Compaction, ScriptFsWriter, ScriptMetaData}
import com.webank.wedatasphere.linkis.storage.utils.{StorageConfiguration, StorageUtils}
import org.apache.commons.io.IOUtils


class StorageScriptFsWriter(val path: FsPath, val charset: String, outputStream: OutputStream = null) extends ScriptFsWriter {

  private val stringBuilder = new StringBuilder

  @scala.throws[IOException]
  override def addMetaData(metaData: MetaData): Unit = {
    val compactions = Compaction.listCompactions().filter(p => p.belongTo(StorageUtils.pathToSuffix(path.getPath)))
    val metadataLine = new util.ArrayList[String]()
    if (compactions.length > 0) {
      metaData.asInstanceOf[ScriptMetaData].getMetaData.map(compactions(0).compact).foreach(metadataLine.add)
      if (outputStream != null) {
        IOUtils.writeLines(metadataLine, "\n", outputStream, charset)
      } else {
        import scala.collection.JavaConversions._
        metadataLine.foreach(m => stringBuilder.append(s"$m\n"))
      }
    }
  }

  @scala.throws[IOException]
  override def addRecord(record: Record): Unit = {
    //转成LineRecord而不是TableRecord是为了兼容非Table类型的结果集写到本类中
    val scriptRecord = record.asInstanceOf[LineRecord]
    if (outputStream != null) {
      IOUtils.write(scriptRecord.getLine, outputStream, charset)
    } else {
      stringBuilder.append(scriptRecord.getLine)
    }
  }

  override def close(): Unit = {
    IOUtils.closeQuietly(outputStream)
  }

  override def flush(): Unit = if (outputStream != null) outputStream.flush()

  def getInputStream(): InputStream = {
    new ByteArrayInputStream(stringBuilder.toString().getBytes(StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue))
  }

}


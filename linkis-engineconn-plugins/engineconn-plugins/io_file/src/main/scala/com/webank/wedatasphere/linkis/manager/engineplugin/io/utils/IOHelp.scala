/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.manager.engineplugin.io.utils

import com.webank.wedatasphere.linkis.common.io.{Fs, FsPath}
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.storage.domain.{MethodEntity, MethodEntitySerializer}
import com.webank.wedatasphere.linkis.storage.exception.StorageErrorException
import com.webank.wedatasphere.linkis.storage.resultset.io.{IOMetaData, IORecord}
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetReader, ResultSetWriter}
import com.webank.wedatasphere.linkis.storage.utils.{StorageConfiguration, StorageUtils}
import org.apache.commons.io.IOUtils

object IOHelp {

  private val maxPageSize = StorageConfiguration.IO_PROXY_READ_FETCH_SIZE.getValue.toLong

  /**
    * 读出内容后，通过将bytes数组转换为base64加密的字符串
    * 现在ujes之前不能直接传输bytes，所以通过base64保真
    * @param fs
    * @param method
    * @return
    */
  def read(fs: Fs, method: MethodEntity): String = {
    if (method.params == null || method.params.isEmpty) throw new StorageErrorException(53002,"The read method parameter cannot be empty(read方法参数不能为空)")
    val dest = MethodEntitySerializer.deserializerToJavaObject(method.params(0).asInstanceOf[String],classOf[FsPath])
    val inputStream = fs.read(dest)
    val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.IO_TYPE)
    val writer = ResultSetWriter.getResultSetWriter(resultSet, Long.MaxValue, null)
    Utils.tryFinally {
      if (method.params.length == 1) {
        val bytes = IOUtils.toByteArray(inputStream)
        val ioMetaData = new IOMetaData(0, bytes.length)
        val ioRecord = new IORecord(bytes)
        writer.addMetaData(ioMetaData)
        writer.addRecord(ioRecord)
        writer.toString()
      } else if (method.params.length == 3) {
        val position = if (method.params(1).toString.toInt < 0) 0 else method.params(1).toString.toInt
        val fetchSize = if (method.params(2).toString.toInt > maxPageSize) maxPageSize.toInt else method.params(2).toString.toInt
        if (position > 0) inputStream.skip(position)
        val bytes = new Array[Byte](fetchSize)
        val len = StorageUtils.readBytes(inputStream,bytes,fetchSize)
        val ioMetaData = new IOMetaData(0, len)
        val ioRecord = new IORecord(bytes.slice(0, len))
        writer.addMetaData(ioMetaData)
        writer.addRecord(ioRecord)
        writer.toString()
      } else throw new StorageErrorException(53003, "Unsupported parameter call(不支持的参数调用)")
    }(IOUtils.closeQuietly(inputStream))
  }

  /**
    * 将穿过来的base64加密的内容转换为bytes数组写入文件
    * @param fs
    * @param method
    */
  def write(fs: Fs, method: MethodEntity): Unit = {
    if (method.params == null || method.params.isEmpty) throw new StorageErrorException(53003, "Unsupported parameter call(不支持的参数调用)")
    val dest = MethodEntitySerializer.deserializerToJavaObject(method.params(0).asInstanceOf[String],classOf[FsPath])
    val overwrite = method.params(1).asInstanceOf[Boolean]
    val outputStream = fs.write(dest, overwrite)
    val content = method.params(2).asInstanceOf[String]
    Utils.tryFinally {
      val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.IO_TYPE)
      val reader = ResultSetReader.getResultSetReader(resultSet, content)
      while (reader.hasNext) {
        IOUtils.write(reader.getRecord.asInstanceOf[IORecord].value, outputStream)
      }
    }(IOUtils.closeQuietly(outputStream))
  }


}

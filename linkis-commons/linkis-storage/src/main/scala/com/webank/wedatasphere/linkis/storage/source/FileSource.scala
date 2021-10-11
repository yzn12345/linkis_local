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

package com.webank.wedatasphere.linkis.storage.source

import java.io.{Closeable, InputStream}
import java.util

import com.webank.wedatasphere.linkis.common.io._
import com.webank.wedatasphere.linkis.storage.exception.StorageErrorException
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetReader}
import com.webank.wedatasphere.linkis.storage.script.ScriptFsReader
import com.webank.wedatasphere.linkis.storage.utils.StorageConfiguration
import org.apache.commons.math3.util.Pair


trait FileSource extends Closeable {

  def shuffle(s: Record => Record): FileSource

  def page(page: Int, pageSize: Int): FileSource

  def collect(): Array[Pair[Object, util.ArrayList[Array[String]]]]

  def write[K <: MetaData, V <: Record](fsWriter: FsWriter[K, V]): Unit

  def addParams(params: util.Map[String, String]): FileSource

  def addParams(key: String, value: String): FileSource

  def getParams: util.Map[String, String]

  def getTotalLine: Int

  def getTypes: Array[String]

  def getFileSplits: Array[FileSplit]
}

object FileSource {

  private val fileType = Array("dolphin", "sql", "scala", "py", "hql", "python", "out", "log", "text", "sh", "jdbc", "ngql", "psql", "fql")
  private val suffixPredicate = (path: String, suffix: String) => path.endsWith(s".$suffix")

  def isResultSet(path: String): Boolean = {
    suffixPredicate(path, fileType.head)
  }

  def isResultSet(fsPath: FsPath): Boolean = {
    isResultSet(fsPath.getPath)
  }

  /**
   * 目前只支持table多结果集
   *
   * @param fsPaths
   * @param fs
   * @return
   */
  def create(fsPaths: Array[FsPath], fs: Fs): FileSource = {
    //非table结果集的过滤掉
    val fileSplits = fsPaths.map(createResultSetFileSplit(_, fs)).filter(isTableResultSet)
    new ResultsetFileSource(fileSplits)
  }

  private def isTableResultSet(fileSplit: FileSplit): Boolean = fileSplit.`type`.equals(ResultSetFactory.TABLE_TYPE)

  def isTableResultSet(fileSource: FileSource): Boolean = {
    //分片中全部为table结果集才返回true
    fileSource.getFileSplits.forall(isTableResultSet)
  }

  def create(fsPath: FsPath, fs: Fs): FileSource = {
    create(fsPath, fs.read(fsPath))
  }

  def create(fsPath: FsPath, is: InputStream): FileSource = {
    if (!canRead(fsPath.getPath)) throw new StorageErrorException(54001, "Unsupported open file type(不支持打开的文件类型)")
    if (isResultSet(fsPath)) {
      new ResultsetFileSource(Array(createResultSetFileSplit(fsPath, is)))
    } else {
      new TextFileSource(Array(createTextFileSplit(fsPath, is)))
    }
  }

  private def createResultSetFileSplit(fsPath: FsPath, fs: Fs): FileSplit = {
    createResultSetFileSplit(fsPath, fs.read(fsPath))
  }

  private def createResultSetFileSplit(fsPath: FsPath, is: InputStream): FileSplit = {
    val resultset = ResultSetFactory.getInstance.getResultSetByPath(fsPath)
    val resultsetReader = ResultSetReader.getResultSetReader(resultset, is)
    new FileSplit(resultsetReader, resultset.resultSetType())
  }

  private def createTextFileSplit(fsPath: FsPath, is: InputStream): FileSplit = {
    val scriptFsReader = ScriptFsReader.getScriptFsReader(fsPath, StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue, is)
    new FileSplit(scriptFsReader)
  }

  private def canRead(path: String): Boolean = {
    fileType.exists(suffixPredicate(path, _))
  }

}


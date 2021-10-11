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

package com.webank.wedatasphere.linkis.jobhistory.util

import java.io.{InputStream, OutputStream}
import java.util.Date
import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.governance.common.entity.job.{JobRequest, SubJobDetail, SubJobInfo}
import com.webank.wedatasphere.linkis.governance.common.entity.task.RequestInsertTask
import com.webank.wedatasphere.linkis.governance.common.protocol.job.JobReqInsert
import com.webank.wedatasphere.linkis.jobhistory.conf.JobhistoryConfiguration
import com.webank.wedatasphere.linkis.jobhistory.entity.{JobHistory, QueryTask}
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.fs.FileSystem
import com.webank.wedatasphere.linkis.storage.utils.{FileSystemUtils, StorageUtils}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.time.DateFormatUtils

object QueryUtils extends Logging {

  private val CODE_STORE_PREFIX = CommonVars("wds.linkis.query.store.prefix", "hdfs:///apps-data/bdp-ide/")
  private val CODE_STORE_PREFIX_VIEW_FS = CommonVars("wds.linkis.query.store.prefix.viewfs", "hdfs:///apps-data/")
  private val IS_VIEW_FS_ENV = CommonVars("wds.linkis.env.is.viewfs", true)
  private val CODE_STORE_SUFFIX = CommonVars("wds.linkis.query.store.suffix", "")
  private val CODE_STORE_LENGTH = CommonVars("wds.linkis.query.code.store.length", 50000)
  private val CHARSET = "utf-8"
  private val CODE_SPLIT = ";"
  private val LENGTH_SPLIT = "#"

  def storeExecutionCode(jobRequest: JobRequest): Unit = {
      storeExecutionCode(jobRequest.getExecuteUser, jobRequest.getExecutionCode, path => jobRequest.setExecutionCode(path))
    }

    def storeExecutionCode(subJobDetail: SubJobDetail, user: String): Unit = {
      storeExecutionCode(user, subJobDetail.getExecutionContent, path => subJobDetail.setExecutionContent(path))
    }

    def storeExecutionCode(user: String, code: String, pathCallback: String => Unit): Unit = {
      if (null == code || code.getBytes().length < CODE_STORE_LENGTH.getValue) return
    val path: String = getCodeStorePath(user)
    val fsPath: FsPath = new FsPath(path)
    val fileSystem = FSFactory.getFsByProxyUser(fsPath, user).asInstanceOf[FileSystem]
    fileSystem.init(null)
    var os: OutputStream = null
    var position = 0L
    val codeBytes = code.getBytes(CHARSET)
    path.intern() synchronized {
      Utils.tryFinally {
        if (!fileSystem.exists(fsPath)) FileSystemUtils.createNewFile(fsPath, user, true)
        os = fileSystem.write(fsPath, false)
        position = fileSystem.get(path).getLength
        IOUtils.write(codeBytes, os)
      } {
        IOUtils.closeQuietly(os)
        if (fileSystem != null) fileSystem.close()
      }
    }
    val length = codeBytes.length
    pathCallback(path + CODE_SPLIT + position + LENGTH_SPLIT + length)
  }

  // todo exchangeExecutionCode for subJobDetail
  def exchangeExecutionCode(queryTask: JobHistory): Unit = {
    import scala.util.control.Breaks._
    if (queryTask.getExecution_code == null || !queryTask.getExecution_code.startsWith(StorageUtils.HDFS_SCHEMA)) return
    val codePath = queryTask.getExecution_code
    val path = codePath.substring(0, codePath.lastIndexOf(CODE_SPLIT))
    val codeInfo = codePath.substring(codePath.lastIndexOf(CODE_SPLIT) + 1)
    val infos: Array[String] = codeInfo.split(LENGTH_SPLIT)
    val position = infos(0).toInt
    var lengthLeft = infos(1).toInt
    val tub = new Array[Byte](1024)
    val executionCode: StringBuilder = new StringBuilder
    val fsPath: FsPath = new FsPath(path)
    val fileSystem = FSFactory.getFsByProxyUser(fsPath, queryTask.getExecute_user).asInstanceOf[FileSystem]
    fileSystem.init(null)
    var is: InputStream = null
    if (!fileSystem.exists(fsPath)) return
    Utils.tryFinally {
      is = fileSystem.read(fsPath)
      if (position > 0) is.skip(position)
      breakable {
        while (lengthLeft > 0) {
          val readed = is.read(tub)
          val useful = Math.min(readed, lengthLeft)
          if (useful < 0) break()
          lengthLeft -= useful
          executionCode.append(new String(tub, 0, useful, CHARSET))
        }
      }
    } {
      IOUtils.closeQuietly(is)
      if (fileSystem != null) Utils.tryAndWarn(fileSystem.close())
    }
    queryTask.setExecution_code(executionCode.toString())
  }

  private def getCodeStorePath(user: String): String = {
    val date: String = DateFormatUtils.format(new Date, "yyyyMMdd")
    if (IS_VIEW_FS_ENV.getValue) {
      s"${CODE_STORE_PREFIX_VIEW_FS.getValue}${user}${CODE_STORE_SUFFIX.getValue}/executionCode/${date}/_scripts"
    }else{
      s"${CODE_STORE_PREFIX.getValue}${user}${CODE_STORE_SUFFIX.getValue}/executionCode/${date}/_scripts"
    }
  }

  def isJobHistoryAdmin(username: String): Boolean = {
    JobhistoryConfiguration.GOVERNANCE_STATION_ADMIN.getValue.split(",").exists(username.equalsIgnoreCase)
  }

  def getJobHistoryAdmin(): Array[String] = {
    JobhistoryConfiguration.GOVERNANCE_STATION_ADMIN.getValue.split(",")
  }
}

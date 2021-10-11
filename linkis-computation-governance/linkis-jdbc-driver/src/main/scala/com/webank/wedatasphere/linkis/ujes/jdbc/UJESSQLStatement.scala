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
package com.webank.wedatasphere.linkis.ujes.jdbc

import java.sql.{Connection, ResultSet, SQLWarning, Statement}
import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.exception.ErrorException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.ujes.client.request.JobExecuteAction
import com.webank.wedatasphere.linkis.ujes.client.request.JobExecuteAction.EngineType
import com.webank.wedatasphere.linkis.ujes.client.response.JobExecuteResult
import com.webank.wedatasphere.linkis.ujes.jdbc.hook.JDBCDriverPreExecutionHook

import scala.collection.JavaConversions
import scala.concurrent.TimeoutException
import scala.concurrent.duration.Duration


class UJESSQLStatement(private[jdbc] val ujesSQLConnection: UJESSQLConnection) extends Statement with Logging{

  private var jobExecuteResult: JobExecuteResult = _
  private var resultSet: UJESSQLResultSet = _
  private var closed = false
  private var maxRows: Int = 0
  private var fetchSize = 100
  private var queryTimeout = 0

  private var queryEnd = false

  private[jdbc] def throwWhenClosed[T](op: => T): T = ujesSQLConnection.throwWhenClosed {
    if(isClosed) throw new UJESSQLException(UJESSQLErrorCode.STATEMENT_CLOSED)
    else op
  }

  override def executeQuery(sql: String): UJESSQLResultSet = {
    if(!execute(sql)) throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_NULL)
    resultSet
  }

  override def executeUpdate(sql: String): Int = {
    execute(sql)
    0
  }

  override def close(): Unit = {
    closed = true
    clearQuery()
  }

  def clearQuery(): Unit = {
    if(jobExecuteResult != null && ! queryEnd) {
      Utils.tryAndWarn(ujesSQLConnection.ujesClient.kill(jobExecuteResult))
      jobExecuteResult = null
    }
    if(resultSet != null) {
      Utils.tryAndWarn(resultSet.close())
      resultSet = null
    }
  }

  override def getMaxFieldSize: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "getMaxFieldSize not supported")

  override def setMaxFieldSize(max: Int): Unit = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "setMaxFieldSize not supported")

  override def getMaxRows: Int = maxRows

  override def setMaxRows(max: Int): Unit = this.maxRows = max

  override def setEscapeProcessing(enable: Boolean): Unit = if(enable) throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "setEscapeProcessing not supported")

  override def getQueryTimeout: Int = queryTimeout

  override def setQueryTimeout(seconds: Int): Unit = throwWhenClosed(queryTimeout = seconds * 1000)

  override def cancel(): Unit = throwWhenClosed(clearQuery())

  override def getWarnings: SQLWarning = null

  override def clearWarnings(): Unit = {}

  override def setCursorName(name: String): Unit = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "setCursorName not supported")

  override def execute(sql: String): Boolean = throwWhenClosed {
    var parsedSQL = sql
    JDBCDriverPreExecutionHook.getPreExecutionHooks.foreach{
      preExecution =>
        parsedSQL = preExecution.callPreExecutionHook(parsedSQL)
    }
    val action = JobExecuteAction.builder().setEngineType(ujesSQLConnection.getEngineType).addExecuteCode(parsedSQL)
      .setCreator(ujesSQLConnection.creator).setUser(ujesSQLConnection.user)

    if(ujesSQLConnection.variableMap.nonEmpty) action.setVariableMap(JavaConversions.mapAsJavaMap(ujesSQLConnection.variableMap))
    jobExecuteResult = ujesSQLConnection.ujesClient.execute(action.build())
    queryEnd = false
    var status = ujesSQLConnection.ujesClient.status(jobExecuteResult)
    val atMost = if(queryTimeout > 0) Duration(queryTimeout, TimeUnit.MILLISECONDS) else Duration.Inf
    if(!status.isCompleted) Utils.tryThrow{
      Utils.waitUntil(() => {
        status = ujesSQLConnection.ujesClient.status(jobExecuteResult)
        status.isCompleted || closed
      }, atMost, 100, 10000)
    } {
      case t: TimeoutException =>
        if(queryTimeout > 0) clearQuery()
        new UJESSQLException(UJESSQLErrorCode.QUERY_TIMEOUT, "query has been timeout!").initCause(t)
      case t => t
    }
    if(!closed) {
      var jobInfo = ujesSQLConnection.ujesClient.getJobInfo(jobExecuteResult)
      if(status.isFailed) throw new ErrorException(jobInfo.getRequestPersistTask.getErrCode,jobInfo.getRequestPersistTask.getErrDesc)
      val jobInfoStatus = jobInfo.getJobStatus
      if(!jobInfoStatus.equals("Succeed")) Utils.tryThrow{
        Utils.waitUntil(() => {
          jobInfo = ujesSQLConnection.ujesClient.getJobInfo(jobExecuteResult)
          val state = jobInfo.getJobStatus match{
            case "Failed" | "Cancelled" | "Timeout" | "Succeed" => true
            case _ => false
          }
          state || closed
        }, atMost, 100, 10000)
      } {
        case t: TimeoutException =>
          if(queryTimeout > 0) clearQuery()
          new UJESSQLException(UJESSQLErrorCode.QUERY_TIMEOUT, "query has been timeout!").initCause(t)
        case t => t
      }
      val resultSetList = jobInfo.getResultSetList(ujesSQLConnection.ujesClient)
      queryEnd = true
      if(resultSetList != null) {
        resultSet = new UJESSQLResultSet(resultSetList, this, maxRows, fetchSize)
        true
      } else false
    } else throw new UJESSQLException(UJESSQLErrorCode.STATEMENT_CLOSED, "Statement is closed.")
  }

  def getJobExcuteResult : JobExecuteResult = jobExecuteResult

  override def getResultSet: UJESSQLResultSet = resultSet

  override def getUpdateCount: Int = throwWhenClosed(-1)

  override def getMoreResults: Boolean = false

  override def setFetchDirection(direction: Int): Unit =
    throwWhenClosed(if(direction != ResultSet.FETCH_FORWARD) throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "only FETCH_FORWARD is supported."))

  override def getFetchDirection: Int = throwWhenClosed(ResultSet.FETCH_FORWARD)

  override def setFetchSize(rows: Int): Unit = this.fetchSize = rows

  override def getFetchSize: Int = fetchSize

  override def getResultSetConcurrency: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "getResultSetConcurrency not supported.")

  override def getResultSetType: Int = throwWhenClosed(ResultSet.TYPE_FORWARD_ONLY)

  override def addBatch(sql: String): Unit = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "addBatch not supported.")

  override def clearBatch(): Unit = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "clearBatch not supported.")

  override def executeBatch(): Array[Int] = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "executeBatch not supported.")

  override def getConnection: Connection = throwWhenClosed(ujesSQLConnection)

  override def getMoreResults(current: Int): Boolean = false

  override def getGeneratedKeys: ResultSet = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "getGeneratedKeys not supported.")

  override def executeUpdate(sql: String, autoGeneratedKeys: Int): Int =
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "executeUpdate with autoGeneratedKeys not supported.")

  override def executeUpdate(sql: String, columnIndexes: Array[Int]): Int =
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "executeUpdate with columnIndexes not supported.")

  override def executeUpdate(sql: String, columnNames: Array[String]): Int =
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "executeUpdate with columnNames not supported.")

  override def execute(sql: String, autoGeneratedKeys: Int): Boolean =
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "execute with autoGeneratedKeys not supported.")

  override def execute(sql: String, columnIndexes: Array[Int]): Boolean =
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "execute with columnIndexes not supported.")

  override def execute(sql: String, columnNames: Array[String]): Boolean =
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "execute with columnNames not supported.")

  override def getResultSetHoldability: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "getResultSetHoldability not supported")

  override def isClosed: Boolean = closed

  override def setPoolable(poolable: Boolean): Unit = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "setPoolable not supported")

  override def isPoolable: Boolean = false

  override def closeOnCompletion(): Unit = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "closeOnCompletion not supported")

  override def isCloseOnCompletion: Boolean = false

  override def unwrap[T](iface: Class[T]): T = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_STATEMENT, "unwrap not supported")

  override def isWrapperFor(iface: Class[_]): Boolean = false
}

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

package com.webank.wedatasphere.linkis.ujes.client

import java.io.Closeable
import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.httpclient.authentication.AuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.dws.config.{DWSClientConfig, DWSClientConfigBuilder}
import com.webank.wedatasphere.linkis.httpclient.response.Result
import com.webank.wedatasphere.linkis.ujes.client.request.JobExecIdAction.JobServiceType
import com.webank.wedatasphere.linkis.ujes.client.request._
import com.webank.wedatasphere.linkis.ujes.client.response._

abstract class UJESClient extends Closeable {

  def execute(jobExecuteAction: JobExecuteAction): JobExecuteResult = executeUJESJob(jobExecuteAction).asInstanceOf[JobExecuteResult]

  def submit(jobSubmitAction: JobSubmitAction): JobSubmitResult = executeUJESJob(jobSubmitAction).asInstanceOf[JobSubmitResult]

  protected[client] def executeUJESJob(ujesJobAction: UJESJobAction): Result

  private def executeJobExecIdAction[T](jobExecuteResult: JobExecuteResult, jobServiceType: JobServiceType.JobServiceType): T = {
    val jobExecIdAction = JobExecIdAction.builder().setJobServiceType(jobServiceType)
      .setExecId(jobExecuteResult.getExecID).setUser(jobExecuteResult.getUser).build()
    executeUJESJob(jobExecIdAction).asInstanceOf[T]
  }

  def status(jobExecuteResult: JobExecuteResult): JobStatusResult = executeJobExecIdAction(jobExecuteResult, JobServiceType.JobStatus)

  def progress(jobExecuteResult: JobExecuteResult): JobProgressResult =
    executeJobExecIdAction(jobExecuteResult, JobServiceType.JobProgress)

  def log(jobExecuteResult: JobExecuteResult, fromLine: Int, size: Int): JobLogResult = {
    val jobLogAction = JobLogAction.builder().setExecId(jobExecuteResult.getExecID)
      .setUser(jobExecuteResult.getUser).setFromLine(fromLine).setSize(size).build()
    executeUJESJob(jobLogAction).asInstanceOf[JobLogResult]
  }

  def list(jobListAction: JobListAction): JobListResult = {
    executeUJESJob(jobListAction).asInstanceOf[JobListResult]
  }

  def log(jobExecuteResult: JobExecuteResult, jobLogResult: JobLogResult): JobLogResult = {
    val jobLogAction = JobLogAction.builder().setExecId(jobExecuteResult.getExecID)
      .setUser(jobExecuteResult.getUser).setFromLine(jobLogResult.getFromLine).build()
    executeUJESJob(jobLogAction).asInstanceOf[JobLogResult]
  }

  def openLog(openLogAction: OpenLogAction): OpenLogResult = {
    executeUJESJob(openLogAction).asInstanceOf[OpenLogResult]
  }

  def kill(jobExecuteResult: JobExecuteResult): JobKillResult = executeJobExecIdAction(jobExecuteResult, JobServiceType.JobKill)

  def pause(jobExecuteResult: JobExecuteResult): JobPauseResult = executeJobExecIdAction(jobExecuteResult, JobServiceType.JobPause)

  def getJobInfo(jobExecuteResult: JobExecuteResult): JobInfoResult = {
    val jobInfoAction = JobInfoAction.builder().setTaskId(jobExecuteResult).build()
    executeUJESJob(jobInfoAction).asInstanceOf[JobInfoResult]
  }

  def resultSet(resultSetAction: ResultSetAction): ResultSetResult =
    executeUJESJob(resultSetAction).asInstanceOf[ResultSetResult]

  def getDBS(getDBSAction: GetDBSAction): GetDBSResult = {
    executeUJESJob(getDBSAction).asInstanceOf[GetDBSResult]
  }

  def getTables(getTableAction: GetTablesAction): GetTablesResult = {
    executeUJESJob(getTableAction).asInstanceOf[GetTablesResult]
  }

  def getColumns(getColumnsAction: GetColumnsAction): GetColumnsResult = {
    executeUJESJob(getColumnsAction).asInstanceOf[GetColumnsResult]
  }

}
object UJESClient {
  def apply(clientConfig: DWSClientConfig): UJESClient = new UJESClientImpl(clientConfig)

  def apply(serverUrl: String): UJESClient = apply(serverUrl, 30000, 10)

  def apply(serverUrl: String, readTimeout: Int, maxConnection: Int): UJESClient =
    apply(serverUrl, readTimeout, maxConnection, new StaticAuthenticationStrategy, "v1")

  def apply(serverUrl: String, readTimeout: Int, maxConnection: Int,
            authenticationStrategy: AuthenticationStrategy, dwsVersion: String): UJESClient = {
    val clientConfig = DWSClientConfigBuilder.newBuilder().addServerUrl(serverUrl)
      .connectionTimeout(30000).discoveryEnabled(false)
        .loadbalancerEnabled(false).maxConnectionSize(maxConnection)
      .retryEnabled(false).readTimeout(readTimeout)
      .setAuthenticationStrategy(authenticationStrategy)
      .setDWSVersion(dwsVersion).build()
    apply(clientConfig)
  }

  def getDiscoveryClient(serverUrl: String): UJESClient = getDiscoveryClient(serverUrl, 30000, 10)

  def getDiscoveryClient(serverUrl: String, readTimeout: Int, maxConnection: Int): UJESClient =
    getDiscoveryClient(serverUrl, readTimeout, maxConnection, new StaticAuthenticationStrategy, "v1")

  def getDiscoveryClient(serverUrl: String, readTimeout: Int, maxConnection: Int,
                         authenticationStrategy: AuthenticationStrategy, dwsVersion: String): UJESClient = {
    val clientConfig = DWSClientConfigBuilder.newBuilder().addServerUrl(serverUrl)
      .connectionTimeout(30000).discoveryEnabled(true)
      .discoveryFrequency(1, TimeUnit.MINUTES)
      .loadbalancerEnabled(true).maxConnectionSize(maxConnection)
      .retryEnabled(false).readTimeout(readTimeout)
      .setAuthenticationStrategy(authenticationStrategy).setDWSVersion(dwsVersion).build()
    apply(clientConfig)
  }
}
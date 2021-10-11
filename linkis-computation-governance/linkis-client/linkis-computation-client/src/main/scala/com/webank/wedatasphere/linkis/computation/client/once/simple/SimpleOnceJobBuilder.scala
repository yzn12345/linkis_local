/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.computation.client.once.simple

import java.io.ByteArrayInputStream
import java.util

import com.webank.wedatasphere.linkis.bml.client.{BmlClient, BmlClientFactory}
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.computation.client.LinkisJobBuilder
import com.webank.wedatasphere.linkis.computation.client.once.LinkisManagerClient
import com.webank.wedatasphere.linkis.computation.client.once.action.CreateEngineConnAction
import com.webank.wedatasphere.linkis.computation.client.once.simple.SimpleOnceJobBuilder._
import com.webank.wedatasphere.linkis.computation.client.utils.LabelKeyUtils
import com.webank.wedatasphere.linkis.governance.common.entity.job.OnceExecutorContent
import com.webank.wedatasphere.linkis.governance.common.utils.OnceExecutorContentUtils
import com.webank.wedatasphere.linkis.governance.common.utils.OnceExecutorContentUtils.BmlResource
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.ujes.client.exception.UJESJobException

import scala.collection.convert.WrapAsJava._
import scala.collection.convert.WrapAsScala._


class SimpleOnceJobBuilder private[simple]()
  extends LinkisJobBuilder[SubmittableSimpleOnceJob] {

  private var createService: String = _
  private var maxSubmitTime: Long = _
  private var description: String = _

  def setCreateService(createService: String): this.type = {
    this.createService = createService
    this
  }

  def setMaxSubmitTime(maxSubmitTime: Long): this.type = {
    this.maxSubmitTime = maxSubmitTime
    this
  }

  def setDescription(description: String): this.type = {
    this.description = description
    this
  }

  protected def getOnceExecutorContent: String = {
    implicit def toMap(map: util.Map[String, Any]): util.Map[String, Object] = map.map { case (k, v) => k -> v.asInstanceOf[Object]}
    val onceExecutorContent = new OnceExecutorContent
    onceExecutorContent.setSourceMap(source.map { case (k, v) => k -> v.asInstanceOf[Object]})
    onceExecutorContent.setVariableMap(TaskUtils.getVariableMap(params))
    onceExecutorContent.setRuntimeMap(TaskUtils.getRuntimeMap(params))
    onceExecutorContent.setJobContent(jobContent)
    onceExecutorContent.setExtraLabels(new util.HashMap[String, Object]) //TODO Set it if needed
    val contentMap = OnceExecutorContentUtils.contentToMap(onceExecutorContent)
    val bytes = DWSHttpClient.jacksonJson.writeValueAsBytes(contentMap)
    val response = getBmlClient.uploadResource(executeUser, getFilePath, new ByteArrayInputStream(bytes))
    OnceExecutorContentUtils.resourceToValue(BmlResource(response.resourceId, response.version))
  }

  override def build(): SubmittableSimpleOnceJob = {
    ensureNotNull(labels, "labels")
    ensureNotNull(jobContent, "jobContent")
    nullThenSet(params){
      params = new util.HashMap[String, Any]
    }
    nullThenSet(source){
      source = new util.HashMap[String, Any]()
    }
    addStartupParam("label." + LabelKeyConstant.CODE_TYPE_KEY, jobContent.get("runType"))
    if(!labels.containsKey(SimpleOnceJobBuilder.ONCE_ENGINE_CONN_MODE_LABEL_KEY))
      addLabel(ONCE_ENGINE_CONN_MODE_LABEL_KEY, ONCE_ENGINE_CONN_MODE_LABEL_VALUE)
    val properties = new util.HashMap[String, String]
    properties.put(OnceExecutorContentUtils.ONCE_EXECUTOR_CONTENT_KEY, getOnceExecutorContent)
    properties.putAll(TaskUtils.getStartupMap(params))
    val createEngineConnAction = CreateEngineConnAction.newBuilder().setCreateService(createService)
      .setLabels(labels.map { case (k, v) => k -> v.asInstanceOf[String]}).setIgnoreTimeout(true)
      .setProperties(properties).setUser(executeUser).setMaxSubmitTime(maxSubmitTime).setDescription(description).build()
    new SubmittableSimpleOnceJob(getLinkisManagerClient, createEngineConnAction)
  }

  implicit def toMap(map: util.Map[String, Any]): util.Map[String, String] = map.map {
    case (k, v: String) => k -> v
    case (k, v) if v != null => k -> v.toString
    case (k, _) => k -> null
  }

  protected def getEngineConnType: String = labels.getOrDefault(LabelKeyUtils.ENGINE_TYPE_LABEL_KEY,
    () => throw new UJESJobException(s"The ${LabelKeyUtils.ENGINE_TYPE_LABEL_KEY} label is necessary.")).asInstanceOf[String]

  protected def getFilePath: String = s"/tmp/$executeUser/$getEngineConnType"

}
object SimpleOnceJobBuilder {
  val ONCE_ENGINE_CONN_MODE_LABEL_KEY = "engineConnMode"
  val ONCE_ENGINE_CONN_MODE_LABEL_VALUE = "once"
  private var bmlClient: BmlClient = _
  private var linkisManagerClient: LinkisManagerClient = _
  def getBmlClient: BmlClient = {
    if(bmlClient == null) synchronized {
      if(bmlClient == null) {
        bmlClient = BmlClientFactory.createBmlClient(LinkisJobBuilder.getDefaultClientConfig)
        Utils.addShutdownHook(() => bmlClient.close())
      }
    }
    bmlClient
  }
  def getLinkisManagerClient: LinkisManagerClient = {
    if(linkisManagerClient == null) synchronized {
      if(linkisManagerClient == null) {
        linkisManagerClient = LinkisManagerClient(LinkisJobBuilder.getDefaultUJESClient)
        Utils.addShutdownHook(() => linkisManagerClient.close())
      }
    }
    linkisManagerClient
  }
}
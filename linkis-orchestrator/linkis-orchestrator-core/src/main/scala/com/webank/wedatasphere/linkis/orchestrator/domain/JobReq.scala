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

package com.webank.wedatasphere.linkis.orchestrator.domain

import java.util

import com.webank.wedatasphere.linkis.manager.label.entity.Label

/**
  *
  */
trait JobReq extends Node {

  def getExecuteUser: String

  def getLabels: util.List[Label[_]]

  def getParams: java.util.Map[String, Any]

  def getPriority: Int

}

abstract class AbstractJobReq extends JobReq {

  private var executeUser: String = _
  private var labels: util.List[Label[_]] = _
  private var params: util.Map[String, Any] = _
  private var priority: Int = _

  override def getExecuteUser: String = executeUser

  override def getLabels: util.List[Label[_]] = labels

  override def getParams: util.Map[String, Any] = params

  override def getPriority: Int = priority

}

object AbstractJobReq {

  abstract class AbstractJobReqBuilder {

    protected val jobReq: AbstractJobReq = createJobReq()

    def clone(jobReq: JobReq) =  {
      setExecuteUser(jobReq.getExecuteUser)
      setLabels(jobReq.getLabels)
      setParams(jobReq.getParams)
      setPriority(jobReq.getPriority)
    }

    def setExecuteUser(executeUser: String) = {
      jobReq.executeUser = executeUser
      this
    }

    def setLabels(labels: util.List[Label[_]])  =  {
      jobReq.labels = labels
      this
    }

    def setParams(params: util.Map[String, Any]) = {
      jobReq.params = params
      this
    }

    def setPriority(priority: Int) =  {
      jobReq.priority = priority
      this
    }

    def build(): JobReq = jobReq

    protected def createJobReq(): AbstractJobReq

  }

}

object JobReq {
  def getDefaultPriority = 0
}
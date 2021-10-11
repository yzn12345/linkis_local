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

package com.webank.wedatasphere.linkis.orchestrator.converter

import java.util

import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.orchestrator.domain.JobReq
import com.webank.wedatasphere.linkis.orchestrator.plans.SimplifyPlanContext
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.{ASTContext, QueryParams, QueryParamsImpl}

/**
  *
  */
class ASTContextImpl extends SimplifyPlanContext with ASTContext {

  private var labels: util.List[Label[_]] = _
  private var params: QueryParams = _
  private var priority: Int = _
  private var executeUser: String = _

  override def getLabels: util.List[Label[_]] = labels

  override def getParams: QueryParams = params

  override def getPriority: Int = priority

  override def getExecuteUser: String = executeUser

}

object ASTContextImpl {

  def newBuilder(): Builder = new Builder

  class Builder {

    private val astContext = new ASTContextImpl

    def setExecuteUser(executeUser: String): Builder = {
      astContext.executeUser = executeUser
      this
    }

    def setPriority(priority: Int): Builder = {
      astContext.priority = priority
      this
    }

    def setParams(params: java.util.Map[String, Any]): Builder = {

      astContext.params = new QueryParamsImpl(params)

      this
    }

    def setLabels(labels: util.List[Label[_]]): Builder = {
      astContext.labels = labels
      this
    }

    def setJobReq(jobReq: JobReq): Builder = setExecuteUser(jobReq.getExecuteUser)
      .setLabels(jobReq.getLabels).setParams(jobReq.getParams).setPriority(jobReq.getPriority)

    def build(): ASTContext = astContext

  }

}
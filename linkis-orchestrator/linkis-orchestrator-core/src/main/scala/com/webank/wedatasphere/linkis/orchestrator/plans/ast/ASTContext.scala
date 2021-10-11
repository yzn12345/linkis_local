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

package com.webank.wedatasphere.linkis.orchestrator.plans.ast

import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.orchestrator.plans.{PlanContext, SimplifyPlanContext}
import java.util

import com.google.common.collect.Lists

/**
  *
  */
trait ASTContext extends PlanContext {

  def getLabels: util.List[Label[_]]

  def getParams: QueryParams

  def getPriority: Int

  def getExecuteUser: String

}

object ASTContext {

  def empty(executeUser: String): ASTContext = new SimplifyPlanContext with ASTContext {
    override def getLabels: util.List[Label[_]] = Lists.newArrayList()

    override def getParams: QueryParams = null

    override def getPriority: Int = 0

    override def getExecuteUser: String = executeUser
  }

}
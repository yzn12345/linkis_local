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

package com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst

import com.webank.wedatasphere.linkis.orchestrator.OrchestratorSession
import com.webank.wedatasphere.linkis.orchestrator.domain.{JobReq, Node, TreeNode}
import com.webank.wedatasphere.linkis.orchestrator.plans.PlanContext
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.{ASTContext, Job}
import com.webank.wedatasphere.linkis.orchestrator.plans.logical.{LogicalContext, Task}
import com.webank.wedatasphere.linkis.orchestrator.plans.physical.{ExecTask, PhysicalContext}

/**
  *
  */
trait Transform[In <: Node, Out <: TreeNode[_], TransformContext <: PlanContext] {

  def apply(in: In, context: TransformContext): Out

  def getName: String

}

/**
  * converterTransform
  */
trait ConverterTransform extends Transform[JobReq, Job, ASTContext]

trait ParserTransform extends Transform[Job, Job, ASTContext]

trait PlannerTransform extends Transform[Job, Task, ASTContext]

trait AnalyzeTransform extends Transform[Task, Task, LogicalContext]

trait OptimizerTransform extends Transform[Task, Task, LogicalContext]

trait PhysicalTransform extends Transform[Task, ExecTask, LogicalContext]

trait ReheaterTransform extends Transform[ExecTask, ExecTask, PhysicalContext]

object Transform {

  type ConverterTransformBuilder = OrchestratorSession => ConverterTransform
  type ParserTransformBuilder = OrchestratorSession => ParserTransform
  type PlannerTransformBuilder = OrchestratorSession => PlannerTransform
  type OptimizerTransformBuilder = OrchestratorSession => OptimizerTransform
  type PhysicalTransformBuilder = OrchestratorSession => PhysicalTransform
  type ReheaterTransformBuilder = OrchestratorSession => ReheaterTransform

}

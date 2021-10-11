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

package com.webank.wedatasphere.linkis.orchestrator.core.impl

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.OrchestratorSession
import com.webank.wedatasphere.linkis.orchestrator.converter.{Converter, ConverterImpl}
import com.webank.wedatasphere.linkis.orchestrator.core.{AbstractSessionState, PlanBuilder}
import com.webank.wedatasphere.linkis.orchestrator.execution.{Execution, ExecutionFactory}
import com.webank.wedatasphere.linkis.orchestrator.extensions.Extensions
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst._
import com.webank.wedatasphere.linkis.orchestrator.extensions.operation.Operation
import com.webank.wedatasphere.linkis.orchestrator.optimizer.{Optimizer, OptimizerImpl}
import com.webank.wedatasphere.linkis.orchestrator.parser.{Parser, ParserImpl}
import com.webank.wedatasphere.linkis.orchestrator.planner.{Planner, PlannerImpl}
import com.webank.wedatasphere.linkis.orchestrator.plans.physical.{ExecTask, PhysicalContext, PhysicalContextImpl}
import com.webank.wedatasphere.linkis.orchestrator.reheater.{Reheater, ReheaterImpl}
import com.webank.wedatasphere.linkis.orchestrator.validator.{Validator, ValidatorImpl}

/**
  *
  */
class SessionStateImpl(orchestratorSession: OrchestratorSession,
                       transforms: Array[Transform[_, _, _]],
                       checkRulers: Array[CheckRuler[_, _]],
                       operations: Array[Operation[_]],
                       extractExtensions: Array[Extensions[_]])
  extends AbstractSessionState(orchestratorSession, transforms, checkRulers, operations, extractExtensions) with Logging {

  private val converter: Converter = createConverter()

  protected def createConverter(): Converter = new ConverterImpl {
    override protected def converterTransforms: Array[ConverterTransform] = super.converterTransforms ++: getConverterTransforms
    override protected def converterCheckRulers: Array[ConverterCheckRuler] = super.converterCheckRulers ++: getConverterCheckRulers
  }

  private val parser: Parser = createParser()

  private val validator: Validator = createValidator()

  private val planner: Planner = createPlanner()

  private val optimizer: Optimizer = createOptimizer()

  private val reheater: Reheater = createReheater()

  private var execution: Execution = _

  override def getParser: Parser = parser

  override def getValidator: Validator = validator

  override def getPlanner: Planner = planner

  override def getOptimizer: Optimizer = optimizer

  override def getConverter: Converter = converter

  protected def createParser(): Parser = new ParserImpl {
    override protected def parserTransforms: Array[ParserTransform] = super.parserTransforms ++: getParserTransforms
  }

  protected def createValidator(): Validator = new ValidatorImpl {
    override protected def validatorCheckRulers: Array[ValidatorCheckRuler] = super.validatorCheckRulers ++: getValidatorCheckRulers
  }

  protected def createPlanner(): Planner = new PlannerImpl {
    override protected def plannerTransforms: Array[PlannerTransform] = super.plannerTransforms ++: getPlannerTransforms
  }

  protected def createOptimizer(): Optimizer = new OptimizerImpl {
    override protected def optimizerTransforms: Array[OptimizerTransform] = super.optimizerTransforms ++: getOptimizerTransforms

    override protected def physicalTransforms: Array[PhysicalTransform] = super.physicalTransforms ++: getPhysicalTransforms

    override protected def createPhysicalContext(execTask: ExecTask, leafNodes: Array[ExecTask]): PhysicalContext = {
      val context = new PhysicalContextImpl(execTask, leafNodes)
      context.setSyncBus(getOrchestratorSyncListenerBus )
      context.setAsyncBus(getOrchestratorAsyncListenerBus)
      context
    }
  }

  override def getExecution: Execution =  {
    if (null == execution) synchronized {
      if (null == execution) {
        execution = ExecutionFactory.getOrCreateExecutionFactory().createExecution(this)
        info(s"Finished to create execution $execution")
      }
    }
    execution
  }

  override def getReheater: Reheater = reheater

  protected def createReheater(): Reheater = new ReheaterImpl {
    override protected def reheaterTransforms: Array[ReheaterTransform] = super.reheaterTransforms ++: getReheaterTransforms
  }

  override def createPlanBuilder(): PlanBuilder = new PlanBuilderImpl
}

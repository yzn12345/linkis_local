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

package com.webank.wedatasphere.linkis.orchestrator.optimizer

import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.{OptimizerTransform, PhysicalTransform}
import com.webank.wedatasphere.linkis.orchestrator.plans.logical.{LogicalContext, LogicalContextImpl, Task}
import com.webank.wedatasphere.linkis.orchestrator.plans.physical.{ExecTask, PhysicalContext, PhysicalContextImpl}

/**
  *
  *
  */
class OptimizerImpl extends AbstractOptimizer {

  override protected def optimizerTransforms: Array[OptimizerTransform] = Array.empty

  override protected def createLogicalContext(task: Task): LogicalContext = {
    val logicalContext = new LogicalContextImpl
    logicalContext
  }

  override protected def physicalTransforms: Array[PhysicalTransform] = Array.empty

  override protected def createPhysicalContext(execTask: ExecTask, leafNodes: Array[ExecTask]): PhysicalContext = new PhysicalContextImpl(execTask, leafNodes)
}

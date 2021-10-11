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

package com.webank.wedatasphere.linkis.orchestrator.extensions.operation

import com.webank.wedatasphere.linkis.orchestrator.OrchestratorSession
import com.webank.wedatasphere.linkis.orchestrator.extensions.OperationExtensions
import com.webank.wedatasphere.linkis.orchestrator.extensions.operation.Operation.OperationBuilder

import scala.collection.mutable.ArrayBuffer

/**
  *
  */
class OperationExtensionsImpl extends OperationExtensions {

  private val operationBuilders = new ArrayBuffer[OperationBuilder]

  override def injectOperation(operationBuilder: OperationBuilder): Unit = operationBuilders += operationBuilder

  override def build(orchestratorSession: OrchestratorSession): Array[Operation[_]] =
    operationBuilders.map(_(orchestratorSession)).toArray
}

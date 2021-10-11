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

package com.webank.wedatasphere.linkis.orchestrator.code.plans.execution

import com.webank.wedatasphere.linkis.common.listener.Event
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.reheater.{Reheater, ReheaterNotifyTaskConsumer}

class CodeReheaterNotifyTaskConsumer(override val reheater: Reheater) extends ReheaterNotifyTaskConsumer with Logging {


  override def onEventError(event: Event, t: Throwable): Unit = {

  }

  override def start(): Unit = {
    val thread = new Thread(this, "CodeReheaterNotifyTaskConsumer")
    thread.start()
    info(s"start consumer ${getClass.getSimpleName} success")
  }

  override def close(): Unit = {

    super.close()
  }
}

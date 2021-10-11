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
 *
 */

package com.webank.wedatasphere.linkis.orchestrator.computation.utils

import com.webank.wedatasphere.linkis.orchestrator.computation.conf.ComputationOrchestratorConf

object ComputationOrchestratorUtils {

  private val len = ComputationOrchestratorConf.LOG_LEN.getValue

  private def limitPrint(log: String): String = {
    if (log.size > len) {
      log.substring(0, len)
    } else {
      log
    }

  }

}

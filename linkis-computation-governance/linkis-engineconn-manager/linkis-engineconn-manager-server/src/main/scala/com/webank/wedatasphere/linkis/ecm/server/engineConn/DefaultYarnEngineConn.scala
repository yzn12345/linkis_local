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

package com.webank.wedatasphere.linkis.ecm.server.engineConn

import com.webank.wedatasphere.linkis.ecm.core.engineconn.YarnEngineConn


class DefaultYarnEngineConn extends DefaultEngineConn with YarnEngineConn {

  var applicationId: String = _

  var applicationURL: String = _

  var yarnMode: String = _

  override def getApplicationId: String = applicationId

  override def setApplicationId(applicationId: String): Unit = this.applicationId = applicationId

  override def getApplicationURL: String = applicationURL

  override def setApplicationURL(applicationURL: String): Unit = this.applicationURL = applicationURL

  override def getYarnMode: String = yarnMode

  override def setYarnMode(yarnMode: String): Unit = this.yarnMode = yarnMode

}

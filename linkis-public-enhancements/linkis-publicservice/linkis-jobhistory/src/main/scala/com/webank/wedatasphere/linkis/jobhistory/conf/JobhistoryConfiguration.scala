/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.jobhistory.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars

object JobhistoryConfiguration {
  //modify this param in linkis.properties
  val GOVERNANCE_STATION_ADMIN = CommonVars("wds.linkis.governance.station.admin", "hadoop")
  val JOB_HISTORY_SAFE_TRIGGER = CommonVars("wds.linkis.jobhistory.safe.trigger", true).getValue

  val ENTRANCE_SPRING_NAME = CommonVars("wds.linkis.entrance.spring.name", "linkis-cg-entrance")
  val ENTRANCE_INSTANCE_DELEMITER = CommonVars("wds.linkis.jobhistory.instance.delemiter", ";")

}

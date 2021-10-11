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

package com.webank.wedatasphere.linkis.metadata.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars


object MdqConfiguration {
  val DEFAULT_STORED_TYPE = CommonVars("bdp.dataworkcloud.datasource.store.type", "orc")
  val DEFAULT_PARTITION_NAME = CommonVars("bdp.dataworkcloud.datasource.default.par.name", "ds")
  val SPARK_MDQ_IMPORT_CLAZZ = CommonVars("wds.linkis.spark.mdq.import.clazz", "com.webank.wedatasphere.linkis.engineplugin.spark.imexport.LoadData")

}

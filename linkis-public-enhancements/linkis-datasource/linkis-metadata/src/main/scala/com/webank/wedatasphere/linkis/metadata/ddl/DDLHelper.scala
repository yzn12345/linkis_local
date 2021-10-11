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

package com.webank.wedatasphere.linkis.metadata.ddl

import java.util

import com.webank.wedatasphere.linkis.common.exception.ErrorException
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.metadata.ddl.ScalaDDLCreator.{CODE, USER}
import com.webank.wedatasphere.linkis.metadata.domain.mdq.bo.MdqTableBO
import org.codehaus.jackson.map.ObjectMapper

object DDLHelper extends Logging {
  def createDDL(params:util.Map[String, Object]):String = {
    val code = if (params.get(CODE) != null) params.get(CODE).toString else throw new ErrorException(58897, "Code from spark Engine is null")
    val user = if (params.get(USER) != null) params.get(USER).toString else {
      logger.warn("User from spark engine is null")
      "hadoop"
    }
    //val mdqTableVO = MdqUtils.gson.fromJson(code, classOf[MdqTableVO])
    val mapper = new ObjectMapper
    val jsonNode = mapper.readTree(code)
    val mdqTableBO = mapper.readValue(jsonNode, classOf[MdqTableBO])
    val importInfo = mdqTableBO.getImportInfo
    if (importInfo != null){
      //如果importInfo 不是空的话，就走的导入hive流程
      ImportDDLCreator.createDDL(mdqTableBO, user)
    } else{
      //如果importInfo是空的话，那么就是走create table 流程
      ScalaDDLCreator.createDDL(mdqTableBO, user)
    }
  }
}

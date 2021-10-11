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

package com.webank.wedatasphere.linkis.metadata.receiver

import java.util

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.metadata.ddl.DDLHelper
import com.webank.wedatasphere.linkis.metadata.domain.mdq.bo.MdqTableBO
import com.webank.wedatasphere.linkis.metadata.service.MdqService
import com.webank.wedatasphere.linkis.metadata.utils.MdqUtils
import com.webank.wedatasphere.linkis.protocol.mdq.{DDLCompleteResponse, DDLExecuteResponse, DDLRequest, DDLResponse}
import com.webank.wedatasphere.linkis.rpc.{Receiver, Sender}
import org.codehaus.jackson.map.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.duration.Duration


@Component
class MDQReceiver extends Receiver with Logging{


  @Autowired
  private var mdqService:MdqService = _

  override def receive(message: Any, sender: Sender): Unit = ???

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case DDLRequest(params:util.Map[String, Object]) =>
      logger.info("received a request from sparkEngine")
      val ddlCode = DDLHelper.createDDL(params)
      DDLResponse(ddlCode)
    case DDLExecuteResponse(status, code, user) =>  if (!status) {
      logger.warn(s"${MdqUtils.ruleString(code)} execute failed")
      DDLCompleteResponse(false)
    }else{
      Utils.tryCatch{
        //存储数据
       // val mdqTableBO = MdqUtils.gson.fromJson(code, classOf[MdqTableBO])
        val mapper = new ObjectMapper
        val jsonNode = mapper.readTree(code)
        val mdqTableBO = mapper.readValue(jsonNode, classOf[MdqTableBO])
        val tableName = mdqTableBO.getTableBaseInfo.getBase.getName
        val dbName = mdqTableBO.getTableBaseInfo.getBase.getDatabase
        logger.info(s"begin to persist table $dbName $tableName")
        mdqService.persistTable(mdqTableBO, user)
        logger.info(s"end to persist table $dbName $tableName")
        DDLCompleteResponse(true)
      }{
        case e:Exception => logger.error(s"fail to persist table for user $user", e)
          DDLCompleteResponse(false)
        case t:Throwable => logger.error(s"fail to persist table for user $user", t)
          DDLCompleteResponse(false)
      }
    }
    case _ => new Object()
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = message match {
    case DDLRequest(params:util.Map[String, Object]) =>
      logger.info("received a request from sparkEngine")
      val ddlCode = DDLHelper.createDDL(params)
      DDLResponse(ddlCode)
    case DDLExecuteResponse(status, code, user) =>  if (!status) {
      logger.warn(s"${MdqUtils.ruleString(code)} execute failed")
      DDLCompleteResponse(false)
    }else{
      Utils.tryCatch{
        //存储数据
        val mapper = new ObjectMapper
        val jsonNode = mapper.readTree(code)
        val mdqTableBO = mapper.readValue(jsonNode, classOf[MdqTableBO])
        val tableName = mdqTableBO.getTableBaseInfo.getBase.getName
        val dbName = mdqTableBO.getTableBaseInfo.getBase.getDatabase
        logger.info(s"begin to persist table $dbName $tableName")
        mdqService.persistTable(mdqTableBO, user)
        DDLCompleteResponse(true)
      }{
        case e:Exception => logger.error(s"fail to persist table for user $user", e)
          DDLCompleteResponse(false)
        case t:Throwable => logger.error(s"fail to persist table for user $user", t)
          DDLCompleteResponse(false)
      }
    }
    case _ => new Object()
  }


}

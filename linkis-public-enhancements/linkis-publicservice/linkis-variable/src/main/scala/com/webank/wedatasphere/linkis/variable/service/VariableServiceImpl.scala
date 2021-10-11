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

package com.webank.wedatasphere.linkis.variable.service

import java.lang.Long
import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.protocol.variable.ResponseQueryVariable
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import com.webank.wedatasphere.linkis.variable.dao.VarMapper
import com.webank.wedatasphere.linkis.variable.entity.{VarKey, VarKeyUser, VarKeyValueVO}
import com.webank.wedatasphere.linkis.variable.exception.VariableException
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VariableServiceImpl extends VariableService with Logging {

  @Autowired
  private var varMapper: VarMapper = _

  override def queryGolbalVariable(userName: String): ResponseQueryVariable = {
    val globals = listGlobalVariable(userName)
    val response = new ResponseQueryVariable
    val map = new util.HashMap[String, String]()
    import scala.collection.JavaConversions._
    globals.foreach(f => map.put(f.getKey, f.getValue))
    response.setKeyAndValue(map)
    response
  }

  override def queryAppVariable(userName: String, creator: String, appName: String): ResponseQueryVariable = {
    val globals = listGlobalVariable(userName)
    val response = new ResponseQueryVariable
    val map = new util.HashMap[String, String]()
    import scala.collection.JavaConversions._
    globals.foreach(f => map.put(f.getKey, f.getValue))
    response.setKeyAndValue(map)
    response
  }

  override def listGlobalVariable(userName: String): util.List[VarKeyValueVO] = {
    varMapper.listGlobalVariable(userName)
  }


  private def removeGlobalVariable(keyID: Long): Unit = {
    val value = varMapper.getValueByKeyID(keyID)
    varMapper.removeKey(keyID)
    varMapper.removeValue(value.getId)
  }

  /*  @Transactional
    override def addGlobalVariable(f: VarKeyValueVO, userName: String): Unit = {
      if (f.getValueID == null || StringUtils.isEmpty(f.getValueID.toString)) {
        val newKey = new VarKey
        newKey.setApplicationID(-1L)
        newKey.setKey(f.getKey)
        varMapper.insertKey(newKey)
        val newValue = new VarKeyUser
        newValue.setApplicationID(-1L)
        newValue.setKeyID(newKey.getId)
        newValue.setUserName(userName)
        newValue.setValue(f.getValue)
        varMapper.insertValue(newValue)
      } else {
        varMapper.updateValue(f.getValueID, f.getValue)
      }
    }*/


  private def insertGlobalVariable(saveVariable: VarKeyValueVO, userName: String): Unit = {
    val newKey = new VarKey
    newKey.setApplicationID(-1L)
    newKey.setKey(saveVariable.getKey)
    varMapper.insertKey(newKey)
    val newValue = new VarKeyUser
    newValue.setApplicationID(-1L)
    newValue.setKeyID(newKey.getId)
    newValue.setUserName(userName)
    newValue.setValue(saveVariable.getValue)
    varMapper.insertValue(newValue)
  }

  private def updateGlobalVariable(saveVariable: VarKeyValueVO, valueID: Long): Unit = {
    varMapper.updateValue(valueID, saveVariable.getValue)
  }

  @Transactional
  override def saveGlobalVaraibles(globalVariables: util.List[_], userVariables: util.List[VarKeyValueVO], userName: String): Unit = {
    import scala.collection.JavaConversions._
    import scala.util.control.Breaks._
    val saves = globalVariables.map(f => BDPJettyServerHelper.gson.fromJson(BDPJettyServerHelper.gson.toJson(f), classOf[VarKeyValueVO]))
    saves.foreach {
      f =>
        if (StringUtils.isBlank(f.getKey) || StringUtils.isBlank(f.getValue)) throw new VariableException("key或value不能为空")
        var flag = true
        breakable {
          for (ele <- userVariables) {
            if (f.getKey.equals(ele.getKey)) {
              flag = false
              updateGlobalVariable(f, ele.getValueID)
              break()
            }
          }
        }
        if (flag) insertGlobalVariable(f, userName)
    }
    userVariables.foreach {
      f =>
        var flag = true
        breakable {
          for (ele <- saves) {
            if (ele.getKey.equals(f.getKey)) {
              flag = false
              break()
            }
          }
          if (flag) removeGlobalVariable(f.getKeyID)
        }
    }

  }
}

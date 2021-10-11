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

package com.webank.wedatasphere.linkis.orchestrator.ecm.entity

import com.webank.wedatasphere.linkis.common.utils.Logging
import java.util

import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineAskRequest
import com.webank.wedatasphere.linkis.manager.label.builder.factory.{LabelBuilderFactoryContext, StdLabelBuilderFactory}
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.entrance.LoadBalanceLabel
import org.apache.commons.lang.StringUtils

import scala.beans.BeanProperty
import scala.collection.JavaConversions._


/**
  *
  *
  */
trait MarkReq {


  def createEngineConnAskReq(): EngineAskRequest

  def getPolicyObj(): Policy

  def setPolicyObj(policy: Policy): Unit

  def registerLabelKey(labelKey: String): Unit

  /**
    * StartUp params
    */
  @BeanProperty
  var properties: util.Map[String, String] = null


  @BeanProperty
  var labels: util.Map[String, AnyRef] = null

  /**
    * executeUser
    */
  @BeanProperty
  var user: String = null


  @BeanProperty
  var createService: String = null

  @BeanProperty
  var description: String = null

  @BeanProperty
  var engineConnCount: Int = _

}

class DefaultMarkReq extends MarkReq with Logging {

  private var policy: Policy = _
  private val labelKeySet: util.Set[String] = new util.HashSet[String]()

  override def createEngineConnAskReq(): EngineAskRequest = {
    val engineAskRequest = new EngineAskRequest
    engineAskRequest.setCreateService(getCreateService)
    engineAskRequest.setDescription(getDescription)
    val labels = new util.HashMap[String, AnyRef]()
    labels.putAll(getLabels.filterKeys(key => key != LabelKeyConstant.BIND_ENGINE_KEY && key != LabelKeyConstant.LOAD_BALANCE_KEY))
    engineAskRequest.setLabels(labels)
    engineAskRequest.setProperties(getProperties)
    engineAskRequest.setUser(getUser)
    engineAskRequest
  }

  override def getPolicyObj(): Policy = this.policy

  override def setPolicyObj(policy: Policy): Unit = this.policy = policy

  override def equals(obj: Any): Boolean = {
    var flag = false
    if (null != obj && obj.isInstanceOf[MarkReq]) {
      val other = obj.asInstanceOf[MarkReq]

      if (other.getUser != getUser) {
        return flag
      }

     /* if (other.getProperties != null && getProperties != null) {
        val iterator = other.getProperties.iterator
        while (iterator.hasNext) {
          val next = iterator.next()
          if (!next._2.equalsIgnoreCase(getProperties.get(next._1))) {
            return flag
          }
        }
      }*/
      if (other.getLabels != null && getLabels != null) {
        val iterator = other.getLabels.iterator
        while (iterator.hasNext) {
          val next = iterator.next()
          if (!getLabels.containsKey(next._1)) {
            return false
          }
          if (null != labelKeySet && labelKeySet.contains(next._1)) {
            val cachedLabel = MarkReq.getLabelBuilderFactory.createLabel[Label[_]](next._1, getLabels.get(next._1))
            val otherLabel = MarkReq.getLabelBuilderFactory.createLabel[Label[_]](next._1, next._2)
            if (!cachedLabel.equals(otherLabel)) {
              return false
            }
          } else {
            if (null == next._2 || !next._2.equals(getLabels.get(next._1))) {
              return false
            }
          }
        }
      }
      flag = true
    }
    flag
  }

  override def hashCode(): Int = {
    getUser.hashCode
  }

  /**
   * Register labelKey that override the equals method, so when compair label in new request with cached labels in markReq,
   * the label with labelKey contained in labelKeySet, would be convert to Label object , and call it's equals method.
   * If you didn't override the equalis method in the label class, please do not register labelKey here.
   * @param labelKey in LabelKeyConstants
   */
  override def registerLabelKey(labelKey: String): Unit = {
    if (StringUtils.isNotBlank(labelKey)) {
      labelKeySet.add(labelKey)
    }
  }

}

class LoadBanlanceMarkReq extends DefaultMarkReq with Logging {

  override def equals(obj: Any): Boolean = {
    var flag = false
    if (null != obj && obj.isInstanceOf[MarkReq]) {
      val other = obj.asInstanceOf[MarkReq]

      if (other.getUser != getUser) {
        return flag
      }
      val loadBalancdLabel = MarkReq.getLabelBuilderFactory.createLabel[LoadBalanceLabel](LabelKeyConstant.LOAD_BALANCE_KEY, getLabels.get(LabelKeyConstant.LOAD_BALANCE_KEY))
      val otherBalancdLabel = MarkReq.getLabelBuilderFactory.createLabel[LoadBalanceLabel](LabelKeyConstant.LOAD_BALANCE_KEY, getLabels.get(LabelKeyConstant.LOAD_BALANCE_KEY))
      if (loadBalancdLabel.getGroupId.equals(otherBalancdLabel.getGroupId)) {
        flag = true
      }

    }
    true
  }

  override def hashCode(): Int = {
    val loadBalancdLabel = MarkReq.getLabelBuilderFactory.createLabel[LoadBalanceLabel](LabelKeyConstant.LOAD_BALANCE_KEY, getLabels.get(LabelKeyConstant.LOAD_BALANCE_KEY))
    if (StringUtils.isNotBlank(loadBalancdLabel.getGroupId)) {
      loadBalancdLabel.getCapacity.hashCode()
    } else {
      getUser.hashCode
    }
  }

}

object MarkReq {

  lazy val labelBuilderFactory =  LabelBuilderFactoryContext.getLabelBuilderFactory

  def getLabelBuilderFactory = labelBuilderFactory

}
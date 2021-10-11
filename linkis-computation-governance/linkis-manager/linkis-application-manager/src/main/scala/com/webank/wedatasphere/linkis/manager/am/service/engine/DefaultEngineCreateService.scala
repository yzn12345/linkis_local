/*
 *
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
package com.webank.wedatasphere.linkis.manager.am.service.engine


import java.util
import java.util.concurrent.{TimeUnit, TimeoutException}

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.exception.LinkisRetryException
import com.webank.wedatasphere.linkis.common.utils.{ByteTimeUtils, Logging, Utils}
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME
import com.webank.wedatasphere.linkis.manager.am.conf.{AMConfiguration, EngineConnConfigurationService}
import com.webank.wedatasphere.linkis.manager.am.exception.{AMErrorCode, AMErrorException}
import com.webank.wedatasphere.linkis.manager.am.label.EngineReuseLabelChooser
import com.webank.wedatasphere.linkis.manager.am.pointer.EngineConnPluginPointer
import com.webank.wedatasphere.linkis.manager.am.selector.NodeSelector
import com.webank.wedatasphere.linkis.manager.common.constant.AMConstant
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.node.{EMNode, EngineNode}
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineCreateRequest, EngineStopRequest}
import com.webank.wedatasphere.linkis.manager.common.utils.ManagerUtils
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.{EngineConnBuildRequestImpl, EngineConnCreationDescImpl}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.resource.TimeoutEngineResourceRequest
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineInstanceLabel, EngineTypeLabel}
import com.webank.wedatasphere.linkis.manager.label.entity.node.AliasServiceInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.entity.{EngineNodeLabel, Label}
import com.webank.wedatasphere.linkis.manager.label.service.{NodeLabelService, UserLabelService}
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtils
import com.webank.wedatasphere.linkis.manager.persistence.{NodeMetricManagerPersistence, ResourceManagerPersistence}
import com.webank.wedatasphere.linkis.manager.service.common.label.{LabelChecker, LabelFilter}
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext
import com.webank.wedatasphere.linkis.resourcemanager.service.ResourceManager
import com.webank.wedatasphere.linkis.resourcemanager.{AvailableResource, NotEnoughResource}
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration

@Service
class DefaultEngineCreateService extends AbstractEngineService with EngineCreateService with Logging {


  @Autowired
  private var nodeSelector: NodeSelector = _

  @Autowired
  private var engineRecycleService: EngineRecycleService = _

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Autowired
  private var resourceManager: ResourceManager = _

  @Autowired
  private var labelCheckerList: util.List[LabelChecker] = _

  @Autowired
  private var labelFilter: LabelFilter = _

  @Autowired
  private var userLabelService: UserLabelService = _

  @Autowired
  private var engineConnConfigurationService: EngineConnConfigurationService = _

  @Autowired
  private var engineConnPluginPointer: EngineConnPluginPointer = _

  @Autowired
  private var nodeMetricManagerPersistence: NodeMetricManagerPersistence = _

  @Autowired
  private var resourceManagerPersistence: ResourceManagerPersistence = _

  @Autowired
  private var engineReuseLabelChoosers: util.List[EngineReuseLabelChooser] = _

  def getEngineNode(serviceInstance: ServiceInstance): EngineNode = {
    val engineNode = getEngineNodeManager.getEngineNode(serviceInstance)
    if (engineNode.getNodeStatus == null){
      engineNode.setNodeStatus(NodeStatus.values()(nodeMetricManagerPersistence.getNodeMetrics(engineNode).getStatus))
    }
    if(engineNode != null) return engineNode
    val labels = resourceManagerPersistence.getLabelsByTicketId(serviceInstance.getInstance)
    labels.foreach { label =>
      LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel[Label[_]](label.getLabelKey, label.getStringValue) match {
        case engineInstanceLabel: EngineInstanceLabel =>
          val serviceInstance = ServiceInstance(engineInstanceLabel.getServiceName, engineInstanceLabel.getInstance)
          return getEngineNodeManager.getEngineNode(serviceInstance)
        case _ =>
      }
    }
    throw new AMErrorException(AMErrorCode.NOT_EXISTS_ENGINE_CONN.getCode, AMErrorCode.NOT_EXISTS_ENGINE_CONN.getMessage)
  }



  @Receiver
  @throws[LinkisRetryException]
  override def createEngine(engineCreateRequest: EngineCreateRequest, smc: ServiceMethodContext): EngineNode = {
    val startTime = System.currentTimeMillis
    info(s"Start to create Engine for request: $engineCreateRequest.")
    val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory
    val timeout = if (engineCreateRequest.getTimeOut <= 0) AMConfiguration.ENGINE_START_MAX_TIME.getValue.toLong else engineCreateRequest.getTimeOut

    // 1. 检查Label是否合法
    var labelList: util.List[Label[_]] = LabelUtils.distinctLabel(labelBuilderFactory.getLabels(engineCreateRequest.getLabels),
      userLabelService.getUserLabels(engineCreateRequest.getUser))

    //label chooser
    if (null != engineReuseLabelChoosers) {
      engineReuseLabelChoosers.foreach { chooser =>
        labelList = chooser.chooseLabels(labelList)
      }
    }


    for (labelChecker <- labelCheckerList) {
      if (!labelChecker.checkEngineLabel(labelList)) {
        throw new AMErrorException(AMConstant.EM_ERROR_CODE, "Need to specify engineType and userCreator label")
      }
    }

    val emLabelList = new util.ArrayList[Label[_]](labelList)
    val emInstanceLabel = labelBuilderFactory.createLabel(classOf[AliasServiceInstanceLabel])
    emInstanceLabel.setAlias(ENGINE_CONN_MANAGER_SPRING_NAME.getValue)
    emLabelList.add(emInstanceLabel)
    //2. NodeLabelService getNodesByLabel  获取EMNodeList
    val emScoreNodeList = getEMService().getEMNodes(emLabelList.filter(!_.isInstanceOf[EngineTypeLabel]))

    //3. 执行Select  比如负载过高，返回没有负载低的EM，每个规则如果返回为空就抛出异常
    val choseNode = if (null == emScoreNodeList || emScoreNodeList.isEmpty) null else nodeSelector.choseNode(emScoreNodeList.toArray)
    if (null == choseNode || choseNode.isEmpty) {
      throw new LinkisRetryException(AMConstant.EM_ERROR_CODE, s" The em of labels${engineCreateRequest.getLabels} not found")
    }
    val emNode = choseNode.get.asInstanceOf[EMNode]
    //4. 请求资源
    val (resourceTicketId, resource) = requestResource(engineCreateRequest, labelFilter.choseEngineLabel(labelList), emNode, timeout)

    //5. 封装engineBuildRequest对象,并发送给EM进行执行
    val engineBuildRequest = EngineConnBuildRequestImpl(
      resourceTicketId,
      labelFilter.choseEngineLabel(labelList),
      resource,
      EngineConnCreationDescImpl(engineCreateRequest.getCreateService, engineCreateRequest.getDescription, engineCreateRequest.getProperties))

    //6. 调用EM发送引擎启动请求调用ASK TODO 异常和等待时间处理
    val engineNode = getEMService().createEngine(engineBuildRequest, emNode)
    info(s"Finished to create  engineConn $engineNode. ticketId is $resourceTicketId")
    //7. 更新持久化信息：包括插入engine/metrics
    //AM会更新serviceInstance表  需要将ticketID进行替换,并更新 EngineConn的Label 需要修改EngineInstanceLabel 中的id为Instance信息
    val oldServiceInstance = new ServiceInstance
    oldServiceInstance.setApplicationName(engineNode.getServiceInstance.getApplicationName)
    oldServiceInstance.setInstance(resourceTicketId)
    getEngineNodeManager.updateEngineNode(oldServiceInstance, engineNode)

    //8. 新增 EngineConn的Label,添加engineConn的Alias
    val engineConnAliasLabel = labelBuilderFactory.createLabel(classOf[AliasServiceInstanceLabel])
    engineConnAliasLabel.setAlias(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue)
    labelList.add(engineConnAliasLabel)
    nodeLabelService.addLabelsToNode(engineNode.getServiceInstance,  labelFilter.choseEngineLabel(LabelUtils.distinctLabel(labelList, fromEMGetEngineLabels(emNode.getLabels))))
    if(System.currentTimeMillis - startTime >= timeout && engineCreateRequest.isIgnoreTimeout) {
      info(s"Return a EngineConn $engineNode for request: $engineCreateRequest since the creator set ignoreTimeout=true and maxStartTime is reached.")
      return engineNode
    }
    Utils.tryCatch {
      val leftWaitTime = timeout - (System.currentTimeMillis - startTime)
      info(s"Start to wait engineConn($engineNode) to be available, but only ${ByteTimeUtils.msDurationToString(leftWaitTime)} left.")
      //9 获取启动的引擎信息，并等待引擎的状态变为IDLE，如果等待超时则返回给用户，并抛出异常
      Utils.waitUntil(() => ensuresIdle(engineNode, resourceTicketId), Duration(leftWaitTime, TimeUnit.MILLISECONDS))
    } {
      case _: TimeoutException =>
        if(!engineCreateRequest.isIgnoreTimeout) {
          info(s"Waiting for $engineNode initialization TimeoutException , now stop it.")
          val stopEngineRequest = new EngineStopRequest(engineNode.getServiceInstance, ManagerUtils.getAdminUser)
          smc.publish(stopEngineRequest)
          throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, s"Waiting for Engine initialization failure, already waiting $timeout ms TicketId ${resourceTicketId}")
        } else {
          warn(s"Waiting for $engineNode initialization TimeoutException, ignore this exception since the creator set ignoreTimeout=true.")
          return engineNode
        }
      case t: Throwable =>
        info(s"Waiting for $engineNode initialization failure , now stop it.")
        val stopEngineRequest = new EngineStopRequest(engineNode.getServiceInstance, ManagerUtils.getAdminUser)
        smc.publish(stopEngineRequest)
        throw t
    }
    info(s"Finished to create Engine for request: $engineCreateRequest and get engineNode $engineNode.")
    engineNode
  }

  private def requestResource(engineCreateRequest: EngineCreateRequest, labelList: util.List[Label[_]], emNode: EMNode, timeout: Long): (String, NodeResource) = {
    //4.  向RM申请对应EM和用户的资源, 抛出资源不足异常：RetryException
    // 4.1 TODO 如果EM资源不足，触发EM回收空闲的engine
    // 4.2 TODO 如果用户资源不足，触发用户空闲的engine回收
    //读取管理台的的配置
    if(engineCreateRequest.getProperties == null) engineCreateRequest.setProperties(new util.HashMap[String,String]())
    val configProp = engineConnConfigurationService.getConsoleConfiguration(labelList)
    val props = engineCreateRequest.getProperties
    if (null != configProp && configProp.nonEmpty) {
      configProp.foreach(keyValue => {
        if (! props.containsKey(keyValue._1)) {
          props.put(keyValue._1, keyValue._2)
        }
      })
    }
    val timeoutEngineResourceRequest = TimeoutEngineResourceRequest(timeout, engineCreateRequest.getUser, labelList, engineCreateRequest.getProperties)
    val resource = engineConnPluginPointer.createEngineResource(timeoutEngineResourceRequest)
    /*  emNode.setLabels(nodeLabelService.getNodeLabels(emNode.getServiceInstance))*/

    resourceManager.requestResource(LabelUtils.distinctLabel(labelList, emNode.getLabels), resource, timeout) match {
      case AvailableResource(ticketId) =>
        (ticketId, resource)
      case NotEnoughResource(reason) =>
        warn(s"资源不足，请重试: $reason")
        throw new LinkisRetryException(AMConstant.EM_ERROR_CODE, s"资源不足，请重试: $reason")
    }
  }

  private def fromEMGetEngineLabels(emLabels: util.List[Label[_]]): util.List[Label[_]] = {
    emLabels.filter { label =>
      label.isInstanceOf[EngineNodeLabel] && !label.isInstanceOf[EngineTypeLabel]
    }
  }

  private def ensuresIdle(engineNode: EngineNode, resourceTicketId: String): Boolean = {
    //TODO 逻辑需要修改，修改为engineConn主动上报
    val engineNodeInfo = Utils.tryAndWarnMsg(getEngineNodeManager.getEngineNodeInfoByDB(engineNode))("Failed to from db get engine node info")
    if (null == engineNodeInfo) return false
    if (NodeStatus.isCompleted(engineNodeInfo.getNodeStatus)) {
      val metrics = nodeMetricManagerPersistence.getNodeMetrics(engineNodeInfo)
      val (reason, canRetry) = getStartErrorInfo(metrics.getHeartBeatMsg)
      if(canRetry.isDefined) {
        throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, s"${engineNode.getServiceInstance} ticketID:$resourceTicketId 初始化引擎失败,原因: ${reason}")
        //throw new AMErrorException(AMConstant.EM_ERROR_CODE, s"初始化引擎失败,原因: ${reason}")
      }
      throw new AMErrorException(AMConstant.EM_ERROR_CODE, s"${engineNode.getServiceInstance} ticketID:$resourceTicketId 初始化引擎失败,原因: ${reason}")
    }
    NodeStatus.isAvailable(engineNodeInfo.getNodeStatus)
  }

  private def getStartErrorInfo(msg: String): (String, Option[Boolean]) = {

    if (StringUtils.isNotBlank(msg)) {
      val jsonNode = BDPJettyServerHelper.jacksonJson.readTree(msg)
      if (jsonNode != null && jsonNode.has(AMConstant.START_REASON)) {
        val startReason = jsonNode.get(AMConstant.START_REASON).asText()
        if (jsonNode.has(AMConstant.EC_CAN_RETRY)) {
          return (startReason, Some(true))
        } else {
          return (startReason, None)
        }
      }
    }
    (null, None)
  }


}

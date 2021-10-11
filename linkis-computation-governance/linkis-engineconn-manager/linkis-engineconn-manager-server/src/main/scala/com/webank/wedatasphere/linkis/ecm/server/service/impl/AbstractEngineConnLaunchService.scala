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

package com.webank.wedatasphere.linkis.ecm.server.service.impl

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.ecm.core.engineconn.{EngineConn, EngineConnInfo}
import com.webank.wedatasphere.linkis.ecm.core.launch._
import com.webank.wedatasphere.linkis.ecm.server.LinkisECMApplication
import com.webank.wedatasphere.linkis.ecm.server.conf.ECMConfiguration._
import com.webank.wedatasphere.linkis.ecm.server.engineConn.DefaultEngineConn
import com.webank.wedatasphere.linkis.ecm.server.hook.{ECMHook, JarUDFLoadECMHook}
import com.webank.wedatasphere.linkis.ecm.server.listener.{EngineConnAddEvent, EngineConnStatusChangeEvent}
import com.webank.wedatasphere.linkis.ecm.server.service.{EngineConnLaunchService, ResourceLocalizationService}
import com.webank.wedatasphere.linkis.ecm.server.util.ECMUtils
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus.Failed
import com.webank.wedatasphere.linkis.manager.common.entity.node.{AMEngineNode, EngineNode}
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineConnStatusCallbackToAM
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import com.webank.wedatasphere.linkis.rpc.Sender
import org.apache.commons.lang.exception.ExceptionUtils

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutorService, Future}
import scala.util.{Failure, Success}


abstract class AbstractEngineConnLaunchService extends EngineConnLaunchService with Logging {


  protected implicit val executor: ExecutionContextExecutorService = Utils.newCachedExecutionContext(ECM_LAUNCH_MAX_THREAD_SIZE, "EngineConn-Manager-Thread-")

  protected var resourceLocalizationService: ResourceLocalizationService = _

  def setResourceLocalizationService(service: ResourceLocalizationService): Unit = this.resourceLocalizationService = service


  def beforeLaunch(request: EngineConnLaunchRequest, conn: EngineConn, duration: Long): Unit = {
    getECMHooks(request).foreach(_.beforeLaunch(request, conn))
  }

  def afterLaunch(request: EngineConnLaunchRequest, conn: EngineConn, duration: Long): Unit = {
    getECMHooks(request).foreach(_.afterLaunch(conn))
  }

  override def launchEngineConn(request: EngineConnLaunchRequest, duration: Long): EngineNode = {
    //1.创建engineConn和runner,launch 并设置基础属性
    val conn = createEngineConn
    val runner = createEngineConnLaunchRunner
    val launch = createEngineConnLaunch
    launch.setEngineConnLaunchRequest(request)
    runner.setEngineConnLaunch(launch)
    conn.setEngineConnLaunchRunner(runner)
    conn.setLabels(request.labels)
    conn.setCreationDesc(request.creationDesc)
    conn.setResource(request.nodeResource)
    conn.setTickedId(request.ticketId)
    conn.setStatus(NodeStatus.Starting)
    conn.setEngineConnInfo(new EngineConnInfo)
    conn.setEngineConnManagerEnv(launch.getEngineConnManagerEnv())
    //2.资源本地化，并且设置ecm的env环境信息
    getResourceLocalizationServie.handleInitEngineConnResources(request, conn)
    //3.添加到list
    LinkisECMApplication.getContext.getECMSyncListenerBus.postToAll(EngineConnAddEvent(conn))
    //4.run
    Utils.tryCatch{
      beforeLaunch(request, conn, duration)
      runner.run()
      launch match {
        case pro: ProcessEngineConnLaunch =>
          val serviceInstance = ServiceInstance(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue, ECMUtils.getInstanceByPort(pro.getEngineConnPort))
          conn.setServiceInstance(serviceInstance)
        case _ =>
      }

      val future = Future {
        afterLaunch(request, conn, duration)
      }

      future onComplete {
        case Failure(t) =>
          throw t
        case Success(_) =>
          info(s"init ${conn.getServiceInstance} succeed.")
      }
      //超时忽略，如果状态翻转了则直接返回
      Utils.tryQuietly(Await.result(future, Duration(WAIT_ENGINECONN_PID.getValue.toLong, TimeUnit.MILLISECONDS)))
    }{
      //failed，1.被ms打断，2.超时，3.普通错误，比如process
      t: Throwable =>
        error(s"init ${conn.getServiceInstance} failed, now stop and delete it. message: ${t.getMessage}")
        conn.getEngineConnLaunchRunner.stop()
        Sender.getSender(MANAGER_SPRING_NAME).send(EngineConnStatusCallbackToAM(conn.getServiceInstance,
          NodeStatus.ShuttingDown, " wait init failed , reason " + ExceptionUtils.getRootCauseMessage(t)))
        LinkisECMApplication.getContext.getECMSyncListenerBus.postToAll(EngineConnStatusChangeEvent(conn.getTickedId, Failed))
        throw t
    }
    val engineNode = new AMEngineNode()
    engineNode.setLabels(conn.getLabels)

    engineNode.setServiceInstance(conn.getServiceInstance)
    engineNode.setOwner(request.user)
    engineNode.setMark("process")
    engineNode
  }

  def createEngineConn: EngineConn = new DefaultEngineConn


  def createEngineConnLaunchRunner: EngineConnLaunchRunner = new EngineConnLaunchRunnerImpl


  def createEngineConnLaunch: EngineConnLaunch


  def getResourceLocalizationServie: ResourceLocalizationService = {
    // TODO: null 抛出异常
    this.resourceLocalizationService
  }

  def getECMHooks(request: EngineConnLaunchRequest): Array[ECMHook] = {
    ECMHook.getECMHooks.filter(h => if (null != request.engineConnManagerHooks) {
      request.engineConnManagerHooks.contains(h.getName)
    } else {
      false
    })
  }

}


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

package com.webank.wedatasphere.linkis.engineplugin.hive.creation

import java.io.{ByteArrayOutputStream, PrintStream}
import java.security.PrivilegedExceptionAction

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.computation.executor.creation.ComputationSingleExecutorEngineConnFactory
import com.webank.wedatasphere.linkis.engineconn.executor.entity.LabelExecutor
import com.webank.wedatasphere.linkis.engineplugin.hive.common.HiveUtils
import com.webank.wedatasphere.linkis.engineplugin.hive.conf.HiveEngineConfiguration
import com.webank.wedatasphere.linkis.engineplugin.hive.entity.HiveSession
import com.webank.wedatasphere.linkis.engineplugin.hive.exception.HiveSessionStartFailedException
import com.webank.wedatasphere.linkis.engineplugin.hive.executor.HiveEngineConnExecutor
import com.webank.wedatasphere.linkis.hadoop.common.utils.HDFSUtils
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineType.EngineType
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType.RunType
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineType, RunType}
import org.apache.hadoop.hive.conf.HiveConf
import org.apache.hadoop.hive.ql.Driver
import org.apache.hadoop.hive.ql.session.SessionState

import scala.collection.JavaConversions._

class HiveEngineConnFactory extends ComputationSingleExecutorEngineConnFactory with Logging {

  private val HIVE_QUEUE_NAME: String = "mapreduce.job.queuename"
  private val BDP_QUEUE_NAME: String = "wds.linkis.rm.yarnqueue"

  override protected def newExecutor(id: Int, engineCreationContext: EngineCreationContext, engineConn: EngineConn): LabelExecutor = {
    engineConn.getEngineConnSession match {
      case hiveSession: HiveSession =>
        new HiveEngineConnExecutor(id, hiveSession.sessionState, hiveSession.ugi, hiveSession.hiveConf, hiveSession.baos)
      case _ =>
        throw HiveSessionStartFailedException(40012, "Failed to create hive executor")
    }
  }

  override protected def createEngineConnSession(engineCreationContext: EngineCreationContext): HiveSession = {
    val options = engineCreationContext.getOptions
    val hiveConf: HiveConf = HiveUtils.getHiveConf
    hiveConf.setVar(HiveConf.ConfVars.HIVEJAR, HiveUtils.jarOfClass(classOf[Driver])
      .getOrElse(throw HiveSessionStartFailedException(40012, "cannot find hive-exec.jar, start session failed!")))
    options.foreach { case (k, v) => info(s"key is $k, value is $v") }
    options.filter { case (k, v) => k.startsWith("hive.") || k.startsWith("mapreduce.") || k.startsWith("mapred.reduce.") || k.startsWith("wds.linkis.") }.foreach { case (k, v) =>
      info(s"key is $k, value is $v")
      if (BDP_QUEUE_NAME.equals(k)) hiveConf.set(HIVE_QUEUE_NAME, v) else hiveConf.set(k, v)
    }
    hiveConf.setVar(HiveConf.ConfVars.HIVE_HADOOP_CLASSPATH, HiveEngineConfiguration.HIVE_LIB_HOME.getValue + "/*")
    if(HiveEngineConfiguration.ENABLE_FETCH_BASE64) {
      hiveConf.setVar(HiveConf.ConfVars.HIVEFETCHOUTPUTSERDE, HiveEngineConfiguration.BASE64_SERDE_CLASS)
      hiveConf.set("enable_fetch_base64","true")
    }
    /* //Update by peaceWong add hook to HiveDriver
     if (StringUtils.isNotBlank(EnvConfiguration.LINKIS_HIVE_POST_HOOKS)) {
       val hooks = if (StringUtils.isNotBlank(hiveConf.get("hive.exec.post.hooks"))) {
         hiveConf.get("hive.exec.post.hooks") + "," + EnvConfiguration.LINKIS_HIVE_POST_HOOKS
       } else {
         EnvConfiguration.LINKIS_HIVE_POST_HOOKS
       }
       hiveConf.set("hive.exec.post.hooks", hooks)
     }*/
    //Update by peaceWong enable hive.stats.collect.scancols
    hiveConf.setBoolean("hive.stats.collect.scancols", true)
    val ugi = HDFSUtils.getUserGroupInformation(Utils.getJvmUser)
    val sessionState: SessionState = ugi.doAs(new PrivilegedExceptionAction[SessionState] {
      override def run(): SessionState = new SessionState(hiveConf)
    })
    val baos = new ByteArrayOutputStream()
    sessionState.out = new PrintStream(baos, true, "utf-8")
    sessionState.info = new PrintStream(System.out, true, "utf-8")
    sessionState.err = new PrintStream(System.out, true, "utf-8")
    SessionState.start(sessionState)

    HiveSession(sessionState, ugi, hiveConf, baos)
  }

  override protected def getEngineConnType: EngineType = EngineType.HIVE

  override protected def getRunType: RunType = RunType.HIVE

}

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

package com.webank.wedatasphere.linkis.engineplugin.spark.executor

import java.util
import java.util.concurrent.atomic.AtomicLong
import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.common.utils.{ByteTimeUtils, Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.{ComputationExecutor, EngineExecutionContext}
import com.webank.wedatasphere.linkis.engineplugin.spark.common.Kind
import com.webank.wedatasphere.linkis.engineplugin.spark.extension.{SparkPostExecutionHook, SparkPreExecutionHook}
import com.webank.wedatasphere.linkis.engineplugin.spark.utils.JobProgressUtil
import com.webank.wedatasphere.linkis.governance.common.exception.LinkisJobRetryException
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.resource._
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.CodeLanguageLabel
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse
import org.apache.spark.SparkContext
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer


abstract class SparkEngineConnExecutor(val sc: SparkContext, id: Long) extends ComputationExecutor with Logging{

  private var initialized: Boolean = false

  private var oldprogress: Float = 0f

  private var jobGroup: String = _

  val queryNum = new AtomicLong(0)



  private var engineExecutionContext: EngineExecutionContext = _

  private var executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]()

  override def init(): Unit = {
    info(s"Ready to change engine state!")
//    setCodeParser()  // todo check
    super.init()
  }



  override def executeLine(engineExecutorContext: EngineExecutionContext, code: String): ExecuteResponse = Utils.tryFinally {
    this.engineExecutionContext = engineExecutorContext
    if (sc.isStopped) {
      error("Spark application has already stopped, please restart it.")
      transition(NodeStatus.Failed)
      throw new LinkisJobRetryException("Spark application sc has already stopped, please restart it.")
    }
    oldprogress = 0f
//    val runType = engineExecutorContext.getProperties.get("runType").asInstanceOf[String]
    val kind: Kind = getKind
    var preCode = code
    engineExecutorContext.appendStdout(LogUtils.generateInfo(s"yarn application id: ${sc.applicationId}"))
    //Pre-execution hook
    Utils.tryQuietly(SparkPreExecutionHook.getSparkPreExecutionHooks().foreach(hook => preCode = hook.callPreExecutionHook(engineExecutorContext, preCode)))
    //Utils.tryAndWarn(CSSparkHelper.setContextIDInfoToSparkConf(engineExecutorContext, sc))
    val _code = Kind.getRealCode(preCode)
    info(s"Ready to run code with kind $kind.")
    jobGroup = String.valueOf("linkis-spark-mix-code-" + queryNum.incrementAndGet())
    //    val executeCount = queryNum.get().toInt - 1
    info("Set jobGroup to " + jobGroup)
    sc.setJobGroup(jobGroup, _code, true)

    val response = Utils.tryFinally(runCode(this, _code, engineExecutorContext, jobGroup)) {
      Utils.tryAndWarn(this.engineExecutionContext.pushProgress(1, getProgressInfo))
      jobGroup = null
      sc.clearJobGroup()
    }
    //Post-execution hook
    Utils.tryQuietly(SparkPostExecutionHook.getSparkPostExecutionHooks().foreach(_.callPostExecutionHook(engineExecutorContext, response, code)))
    response
  } {
    this.engineExecutionContext = null
    oldprogress = 0f
  }

  override def executeCompletely(engineExecutorContext: EngineExecutionContext, code: String, completedLine: String): ExecuteResponse = {
    val newcode = completedLine + code
    info("newcode is " + newcode)
    executeLine(engineExecutorContext, newcode)
  }

  override def progress(): Float = if (jobGroup == null || engineExecutionContext.getTotalParagraph == 0) 0
  else {
    debug("request new progress for jobGroup is " + jobGroup + "old progress:" + oldprogress)
    val newProgress = (engineExecutionContext.getCurrentParagraph * 1f - 1f )/ engineExecutionContext.getTotalParagraph + JobProgressUtil.progress(sc,jobGroup)/engineExecutionContext.getTotalParagraph - 0.01f
    if(newProgress < oldprogress && oldprogress < 0.98) oldprogress else {
      oldprogress = newProgress
      newProgress
    }
  }

  override def getProgressInfo: Array[JobProgressInfo] = if (jobGroup == null) Array.empty
  else {
    debug("request new progress info for jobGroup is " + jobGroup)
    val progressInfoArray = ArrayBuffer[JobProgressInfo]()
    progressInfoArray ++= JobProgressUtil.getActiveJobProgressInfo(sc,jobGroup)
    progressInfoArray ++= JobProgressUtil.getCompletedJobProgressInfo(sc,jobGroup)
    progressInfoArray.toArray
  }

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = this.executorLabels = labels

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource = {
    // todo check
    null
  }

  override def getCurrentNodeResource(): NodeResource = {
    info("Begin to get actual used resources!")
    Utils.tryCatch({
      //      val driverHost: String = sc.getConf.get("spark.driver.host")
      //      val executorMemList = sc.getExecutorMemoryStatus.filter(x => !x._1.split(":")(0).equals(driverHost)).map(x => x._2._1)
      val executorNum: Int = sc.getConf.get("spark.executor.instances").toInt
      val executorMem: Long = ByteTimeUtils.byteStringAsBytes(sc.getConf.get("spark.executor.memory")) * executorNum

      //      if(executorMemList.size>0) {
      //        executorMem = executorMemList.reduce((x, y) => x + y)
      //      }
      val driverMem: Long = ByteTimeUtils.byteStringAsBytes(sc.getConf.get("spark.driver.memory"))
      //      val driverMemList = sc.getExecutorMemoryStatus.filter(x => x._1.split(":")(0).equals(driverHost)).map(x => x._2._1)
      //      if(driverMemList.size > 0) {
      //          driverMem = driverMemList.reduce((x, y) => x + y)
      //      }
      val sparkExecutorCores = sc.getConf.get("spark.executor.cores", "2").toInt * executorNum
      val sparkDriverCores = sc.getConf.get("spark.driver.cores", "1").toInt
      val queue = sc.getConf.get("spark.yarn.queue")
      info("Current actual used resources is driverMem:" + driverMem + ",driverCores:" + sparkDriverCores + ",executorMem:" + executorMem + ",executorCores:" + sparkExecutorCores + ",queue:" + queue)
      val uesdResource = new DriverAndYarnResource(
        new LoadInstanceResource(driverMem, sparkDriverCores, 1),
        new YarnResource(executorMem, sparkExecutorCores, 0, queue, sc.applicationId)
      )
      val nodeResource = new CommonNodeResource
      nodeResource.setUsedResource(uesdResource)
      nodeResource
    })(t => {
      warn("Get actual used resource exception", t)
      null
    })
  }

  override def supportCallBackLogs(): Boolean = {
    // todo
    true
  }

  override def getId(): String = getExecutorIdPreFix + id


  protected def getExecutorIdPreFix: String

  protected def getKind: Kind

  protected def runCode(executor: SparkEngineConnExecutor, code: String, context: EngineExecutionContext, jobGroup: String): ExecuteResponse

  override def killTask(taskID: String): Unit = {
    if (!sc.isStopped) {
      sc.cancelAllJobs
      killRunningTask()
    }
    super.killTask(taskID)
  }

  protected def killRunningTask(): Unit = {
    var runType : String = ""
    getExecutorLabels().asScala.foreach {l => l match {
        case label: CodeLanguageLabel =>
          runType = label.getCodeType
        case _ =>
      }
    }
    warn(s"Kill running job of ${runType} .")
  }

  override def close(): Unit = {
    super.close()
  }
}


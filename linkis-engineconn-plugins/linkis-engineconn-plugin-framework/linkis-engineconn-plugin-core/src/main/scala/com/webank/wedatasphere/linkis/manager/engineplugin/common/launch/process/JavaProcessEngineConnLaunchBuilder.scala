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

package com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.process


import java.io.File
import java.nio.file.Paths
import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.protocol.bml.BmlResource
import com.webank.wedatasphere.linkis.manager.engineplugin.common.conf.{EngineConnPluginConf, EnvConfiguration}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.conf.EnvConfiguration.LINKIS_PUBLIC_MODULE_PATH
import com.webank.wedatasphere.linkis.manager.engineplugin.common.exception.EngineConnBuildFailedException
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.{EngineConnBuildRequest, RicherEngineConnBuildRequest}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.process.Environment.{variable, _}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.process.LaunchConstants._
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineTypeLabel
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.time.DateFormatUtils

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer


abstract class JavaProcessEngineConnLaunchBuilder extends ProcessEngineConnLaunchBuilder with Logging {

  private var engineConnResourceGenerator: EngineConnResourceGenerator = _

  def setEngineConnResourceGenerator(engineConnResourceGenerator: EngineConnResourceGenerator): Unit =
    this.engineConnResourceGenerator = engineConnResourceGenerator

  protected def getGcLogDir(engineConnBuildRequest: EngineConnBuildRequest): String = variable(LOG_DIRS) + "/gc.log"

  protected def getLogDir(engineConnBuildRequest: EngineConnBuildRequest): String = s" -Dlogging.file=${EnvConfiguration.LOG4J2_XML_FILE.getValue} " +
    s" -D$TICKET_ID_KEY=${engineConnBuildRequest.ticketId}"

  override protected def getCommands(implicit engineConnBuildRequest: EngineConnBuildRequest): Array[String] = {
    val commandLine: ArrayBuffer[String] = ArrayBuffer[String]()
    commandLine += (variable(JAVA_HOME) + "/bin/java")
    commandLine += "-server"
    val engineConnMemory = EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.getValue.toString
    commandLine += ("-Xmx" + engineConnMemory)
    commandLine += ("-Xms" + engineConnMemory)
    val javaOPTS = getExtractJavaOpts
    if (StringUtils.isNotEmpty(EnvConfiguration.ENGINE_CONN_DEFAULT_JAVA_OPTS.getValue))
      EnvConfiguration.ENGINE_CONN_DEFAULT_JAVA_OPTS.getValue.format(getGcLogDir(engineConnBuildRequest)).split("\\s+").foreach(commandLine += _)
    if (StringUtils.isNotEmpty(javaOPTS)) javaOPTS.split("\\s+").foreach(commandLine += _)
    getLogDir(engineConnBuildRequest).trim.split(" ").foreach(commandLine += _)
    commandLine += ("-Djava.io.tmpdir=" + variable(TEMP_DIRS))
    if (EnvConfiguration.ENGINE_CONN_DEBUG_ENABLE.getValue) {
      commandLine += s"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${variable(RANDOM_PORT)}"
    }
    commandLine += "-cp"
    commandLine += variable(CLASSPATH)
    commandLine += getMainClass
    commandLine ++= Seq("1>", s"${variable(LOG_DIRS)}/stdout", "2>>", s"${variable(LOG_DIRS)}/stderr")
    commandLine.toArray
  }

  protected def getMainClass: String = EngineConnPluginConf.ENGINECONN_MAIN_CLASS.getValue

  override protected def getEnvironment(implicit engineConnBuildRequest: EngineConnBuildRequest): util.Map[String, String] = {
    info("Setting up the launch environment for engineconn.")
    val environment = new util.HashMap[String, String]
    if(ifAddHiveConfigPath) {
      addPathToClassPath(environment, variable(HADOOP_CONF_DIR))
      addPathToClassPath(environment, variable(HIVE_CONF_DIR))
    }
//    addPathToClassPath(environment, variable(PWD))
    // first, add engineconn conf dirs.
    addPathToClassPath(environment, Seq(variable(PWD), ENGINE_CONN_CONF_DIR_NAME))
    // second, add engineconn libs.
    addPathToClassPath(environment, Seq(variable(PWD), ENGINE_CONN_LIB_DIR_NAME + "/*"))
    // then, add public modules.
    if (!enablePublicModule) {
      addPathToClassPath(environment, Seq(LINKIS_PUBLIC_MODULE_PATH.getValue + "/*"))
    }
    // finally, add the suitable properties key to classpath
    engineConnBuildRequest.engineConnCreationDesc.properties.foreach { case (key, value) =>
      if (key.startsWith("engineconn.classpath") || key.startsWith("wds.linkis.engineconn.classpath")) {
        addPathToClassPath(environment, Seq(variable(PWD), new File(value).getName))
      }
    }
    getExtraClassPathFile.foreach { file: String =>
      addPathToClassPath(environment, Seq(variable(PWD), new File(file).getName))
    }
    engineConnBuildRequest match {
      case richer: RicherEngineConnBuildRequest =>
        def addFiles(files: String): Unit = if (StringUtils.isNotBlank(files)) {
          files.split(",").foreach(file => addPathToClassPath(environment, Seq(variable(PWD), new File(file).getName)))
        }

        val configs: util.Map[String, String] = richer.getStartupConfigs.filter(_._2.isInstanceOf[String]).map { case (k, v: String) => k -> v }
        val jars: String = EnvConfiguration.ENGINE_CONN_JARS.getValue(configs)
        addFiles(jars)
        val files: String = EnvConfiguration.ENGINE_CONN_CLASSPATH_FILES.getValue(configs)
        addFiles(files)
      case _ =>
    }
    environment
  }

  override protected def getNecessaryEnvironment(implicit engineConnBuildRequest: EngineConnBuildRequest): Array[String] =
    if(!ifAddHiveConfigPath) Array.empty else Array(HADOOP_CONF_DIR.toString, HIVE_CONF_DIR.toString)

  protected def getExtractJavaOpts: String = EnvConfiguration.ENGINE_CONN_JAVA_EXTRA_OPTS.getValue

  protected def getExtraClassPathFile: Array[String] = EnvConfiguration.ENGINE_CONN_JAVA_EXTRA_CLASSPATH.getValue.split(",")

  protected def ifAddHiveConfigPath: Boolean = false

  protected def enablePublicModule: Boolean = false

  override protected def getBmlResources(implicit engineConnBuildRequest: EngineConnBuildRequest): util.List[BmlResource] = {
    val engineType = engineConnBuildRequest.labels.find(_.isInstanceOf[EngineTypeLabel])
      .map{ case engineTypeLabel: EngineTypeLabel => engineTypeLabel}.getOrElse(throw new EngineConnBuildFailedException(20000, "EngineTypeLabel is not exists."))
    val engineConnResource = engineConnResourceGenerator.getEngineConnBMLResources(engineType)
    Array(engineConnResource.getConfBmlResource, engineConnResource.getLibBmlResource) ++: engineConnResource.getOtherBmlResources.toList
  }

  private implicit def buildPath(paths: Seq[String]): String = Paths.get(paths.head, paths.tail: _*).toFile.getPath

}
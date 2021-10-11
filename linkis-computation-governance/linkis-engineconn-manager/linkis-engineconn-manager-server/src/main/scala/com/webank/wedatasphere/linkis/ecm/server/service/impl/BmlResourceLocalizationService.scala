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

import java.io.File
import java.nio.file.Paths
import com.webank.wedatasphere.linkis.DataWorkCloudApplication
import com.webank.wedatasphere.linkis.common.conf.Configuration
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.{Utils, ZipUtils}
import com.webank.wedatasphere.linkis.ecm.core.engineconn.EngineConn
import com.webank.wedatasphere.linkis.ecm.core.launch.EngineConnManagerEnv
import com.webank.wedatasphere.linkis.ecm.server.conf.ECMConfiguration._
import com.webank.wedatasphere.linkis.ecm.server.service.{LocalDirsHandleService, ResourceLocalizationService}
import com.webank.wedatasphere.linkis.ecm.server.util.ECMUtils
import com.webank.wedatasphere.linkis.manager.common.protocol.bml.BmlResource
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.process.ProcessEngineConnLaunchRequest
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.fs.FileSystem
import com.webank.wedatasphere.linkis.storage.utils.{FileSystemUtils, StorageUtils}

import scala.collection.JavaConversions._
import scala.collection.mutable


class BmlResourceLocalizationService extends ResourceLocalizationService {

  private implicit val fs: FileSystem = FSFactory.getFs(StorageUtils.FILE).asInstanceOf[FileSystem]

  fs.init(null)

  private val seperator = File.separator

  private val schema = StorageUtils.FILE_SCHEMA

  private var localDirsHandleService: LocalDirsHandleService = _

  def setLocalDirsHandleService(localDirsHandleService: LocalDirsHandleService): Unit = this.localDirsHandleService = localDirsHandleService

  override def handleInitEngineConnResources(request: EngineConnLaunchRequest, engineConn: EngineConn): Unit = {
    // TODO: engineType判断是否下载到本地 unzip
    //engine_type resourceId version判断是否更新，或者重新下载，将path给到properties
    request match {
      case request: ProcessEngineConnLaunchRequest =>
        val files = request.bmlResources
        val linkDirsP = new mutable.HashMap[String, String]
        val user = request.user
        val ticketId = request.ticketId
        val workDir = createDirIfNotExit(localDirsHandleService.getEngineConnWorkDir(user, ticketId))
        val emHomeDir = createDirIfNotExit(localDirsHandleService.getEngineConnManagerHomeDir)
        val logDirs = createDirIfNotExit(localDirsHandleService.getEngineConnLogDir(user, ticketId))
        val tmpDirs = createDirIfNotExit(localDirsHandleService.getEngineConnTmpDir(user, ticketId))
        files.foreach(downloadBmlResource(request, linkDirsP, _, workDir))
        engineConn.getEngineConnLaunchRunner.getEngineConnLaunch.setEngineConnManagerEnv(new EngineConnManagerEnv {
          override val engineConnManagerHomeDir: String = emHomeDir
          override val engineConnWorkDir: String = workDir
          override val engineConnLogDirs: String = logDirs
          override val engineConnTempDirs: String = tmpDirs

          var hostName = Utils.getComputerName
          val eurekaPreferIp = Configuration.EUREKA_PREFER_IP
          if(eurekaPreferIp){
            hostName = DataWorkCloudApplication.getApplicationContext.getEnvironment().getProperty("spring.cloud.client.ip-address")
          }
          override val engineConnManagerHost: String = hostName
          override val engineConnManagerPort: String = DataWorkCloudApplication.getApplicationContext.getEnvironment.getProperty("server.port")
          override val linkDirs: Map[String, String] = linkDirsP.toMap
          // TODO: 注册发现信息的配置化
          override val properties: Map[String, String] = Map("eureka.client.serviceUrl.defaultZone" -> ECM_EUREKA_DEFAULTZONE)
        })
      case _ =>
    }
  }


  private val bmlResourceSuffix = ".zip"

  private def createDirIfNotExit(noSchemaPath: String): String = {
    val fsPath = new FsPath(schema + noSchemaPath)
    if (!fs.exists(fsPath)) {
      FileSystemUtils.mkdirs(fs, fsPath, Utils.getJvmUser)
      fs.setPermission(fsPath, "rwxrwxrwx")
    }
    noSchemaPath
  }

  def downloadBmlResource(request: ProcessEngineConnLaunchRequest, linkDirs: mutable.HashMap[String, String], resource: BmlResource, workDir: String): Unit = {
    val resourceId = resource.getResourceId
    val version = resource.getVersion
    val user = request.user
    resource.getVisibility match {
      case BmlResource.BmlResourceVisibility.Public =>
        val publicDir = localDirsHandleService.getEngineConnPublicDir
        val bmlResourceDir = schema + Paths.get(publicDir, resourceId, version).toFile.getPath
        val fsPath = new FsPath(bmlResourceDir)
        if (!fs.exists(fsPath)) {
          ECMUtils.downLoadBmlResourceToLocal(resource, user, fsPath.getPath)
          val unzipDir = fsPath.getSchemaPath + File.separator + resource.getFileName.substring(0, resource.getFileName.lastIndexOf("."))
          FileSystemUtils.mkdirs(fs, new FsPath(unzipDir), Utils.getJvmUser)
          ZipUtils.unzip(bmlResourceDir + File.separator + resource.getFileName, unzipDir)
          fs.delete(new FsPath(bmlResourceDir + File.separator + resource.getFileName))
        }
        //2.软连，并且添加到map
        val dirAndFileList = fs.listPathWithError(fsPath)
        dirAndFileList.getFsPaths.foreach {
          case path: FsPath =>
            val name = new File(path.getPath).getName
            linkDirs.put(path.getPath, workDir + seperator + name)
        }
      case BmlResource.BmlResourceVisibility.Private =>
        val fsPath = new FsPath(schema + workDir)
        if (!fs.exists(fsPath)) {
          FileSystemUtils.mkdirs(fs, fsPath, Utils.getJvmUser)
          ECMUtils.downLoadBmlResourceToLocal(resource, user, fsPath.getPath)
          ZipUtils.unzip(schema + workDir + File.separator + resource.getFileName, fsPath.getSchemaPath)
          fs.delete(new FsPath(schema + workDir + File.separator + resource.getFileName))
        }
      case BmlResource.BmlResourceVisibility.Label =>
    }
  }

}

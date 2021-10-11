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

package com.webank.wedatasphere.linkis.engineplugin.server.localize

import java.io.{File, FileInputStream, InputStream}

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils, ZipUtils}
import com.webank.wedatasphere.linkis.engineplugin.server.localize.EngineConnBmlResourceGenerator.NO_VERSION_MARK
import com.webank.wedatasphere.linkis.manager.engineplugin.common.exception.EngineConnPluginErrorException


class DefaultEngineConnBmlResourceGenerator extends AbstractEngineConnBmlResourceGenerator with Logging {

  override def generate(engineConnType: String): Map[String, Array[EngineConnLocalizeResource]] =

    getEngineConnDistHomeList(engineConnType).map { path =>
      val distFile = new File(path)
      val key = if(distFile.getName.startsWith("v")) distFile.getName else NO_VERSION_MARK
      Utils.tryCatch {
        key -> generateDir(path)
      } {
        case t: Throwable =>
          error(s"Generate dir : $path error, msg : " + t.getMessage, t)
          throw t
      }
    }.toMap

  override def generate(engineConnType: String, version: String): Array[EngineConnLocalizeResource] = {
    val path = getEngineConnDistHome(engineConnType, version)
    generateDir(path)
  }

  private def generateDir(path: String): Array[EngineConnLocalizeResource] = {
    val distFile = new File(path)
    val validFiles = distFile.listFiles().filterNot(f => f.getName.endsWith(".zip") &&
      new File(path, f.getName.replace(".zip", "")).exists)
     validFiles.map { file =>
      if(file.isFile)
        EngineConnLocalizeResourceImpl(file.getPath, file.getName, file.lastModified(), file.length())
          .asInstanceOf[EngineConnLocalizeResource]
      else {
        val newFile = new File(path, file.getName + ".zip")
        if(newFile.exists() && !newFile.delete()) {
          throw new EngineConnPluginErrorException(20001, s"System have no permission to delete old engineConn file $newFile.")
        }
        ZipUtils.fileToZip(file.getPath, path, file.getName + ".zip")
        // 如果是文件夹，这里的最后更新时间，采用文件夹的最后更新时间，而不是ZIP的最后更新时间.
        EngineConnLocalizeResourceImpl(newFile.getPath, newFile.getName, file.lastModified(), newFile.length())
          .asInstanceOf[EngineConnLocalizeResource]
      }
    }
  }
}
case class EngineConnLocalizeResourceImpl(filePath: String, fileName: String, lastModified: Long, fileSize: Long)
  extends EngineConnLocalizeResource {
  override def getFileInputStream: InputStream = new FileInputStream(filePath)
}
/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.bml.http

import com.webank.wedatasphere.linkis.bml.conf.BmlConfiguration
import com.webank.wedatasphere.linkis.common.conf.Configuration

object HttpConf {

  val gatewayInstance: String = Configuration.getGateWayURL()
  val urlPrefix: String = if (BmlConfiguration.URL_PREFIX.getValue.endsWith("/")) {
    BmlConfiguration.URL_PREFIX.getValue.substring(0, BmlConfiguration.URL_PREFIX.getValue.length - 1)
  } else BmlConfiguration.URL_PREFIX.getValue

  val uploadURL: String = urlPrefix + "/upload"
  val downloadURL: String = urlPrefix + "/download"
  val downloadShareURL: String = urlPrefix + "/" + "downloadShareResource"
  val updateProjectUrl: String = urlPrefix + "/" + "updateProjectUsers"
  val deleteURL: String = urlPrefix + "/delete"
  val updateVersionURL: String = urlPrefix + "/updateVersion"
  val relateHdfsURL:String = gatewayInstance + urlPrefix + "/relateHdfs"
  val relateStorageURL:String = gatewayInstance + urlPrefix + "/relateStorage"
  val getResourcesUrl:String = gatewayInstance + urlPrefix + "/getResourceMsg"
  val updateBasicUrl:String = gatewayInstance + urlPrefix + "/updateBasic"
  val getVersionsUrl:String = gatewayInstance + urlPrefix + "/getVersions"
  val getBasicUrl:String = gatewayInstance + urlPrefix + "/getBasic"
  val createProjectUrl:String = urlPrefix + "/" + "createBmlProject"
  val uploadShareResourceUrl:String = urlPrefix + "/" + "uploadShareResource"
  val updateShareResourceUrl:String = urlPrefix + "/" + "updateShareResource"
  val attachUrl:String = urlPrefix + "/" + "attachResourceAndProject"
  def main(args: Array[String]): Unit = {
    println(uploadURL)
    println(downloadURL)
    println(updateVersionURL)
    println(relateHdfsURL)
    println(relateStorageURL)
    println(getResourcesUrl)
    println(updateBasicUrl)
  }


}

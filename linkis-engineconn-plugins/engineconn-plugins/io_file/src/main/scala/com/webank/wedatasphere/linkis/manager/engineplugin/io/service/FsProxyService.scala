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

package com.webank.wedatasphere.linkis.manager.engineplugin.io.service

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.storage.utils.{StorageConfiguration, StorageUtils}

class FsProxyService extends Logging{

  def canProxyUser(creatorUser:String, proxyUser:String, fsType:String): Boolean = creatorUser match {
    case StorageConfiguration.STORAGE_ROOT_USER.getValue => true
    case StorageConfiguration.LOCAL_ROOT_USER.getValue => StorageUtils.FILE == fsType
    case StorageConfiguration.HDFS_ROOT_USER.getValue => StorageUtils.HDFS == fsType
    case _ => true//creatorUser.equals(proxyUser)
  }
  /*  if(creatorUser.equals(proxyUser)){
     return true
    }
    if(creatorUser == StorageConfiguration.STORAGE_ROOT_USER.getValue ) return t
    if(StorageUtils.FILE == fsType && creatorUser == StorageConfiguration.LOCAL_ROOT_USER.getValue){
      return true
    }
    if(StorageUtils.HDFS == fsType && creatorUser == StorageConfiguration.HDFS_ROOT_USER.getValue){
      return true
    }
    info(s"$creatorUser Failed to proxy user:$proxyUser of FsType:$fsType")
     true*/

}

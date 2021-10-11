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


package com.webank.wedatasphere.linkis.hadoop.common.entity

import com.webank.wedatasphere.linkis.hadoop.common.conf.HadoopConf
import org.apache.hadoop.fs.FileSystem

class HDFSFileSystemContainer(fs: FileSystem, user: String) {

  private var lastAccessTime: Long = System.currentTimeMillis()

  private var  count: Int = 0

  def getFileSystem: FileSystem = this.fs

  def getUser: String = this.user

  def getLastAccessTime: Long = this.lastAccessTime

  def updateLastAccessTime: Unit = {
    this.lastAccessTime = System.currentTimeMillis()
  }

  def addAccessCount(): Unit = {
    count = count + 1
  }

  def minusAccessCount(): Unit = count = count -1

  def canRemove(): Boolean = {
    val currentTime = System.currentTimeMillis()
    val idleTime = currentTime - this.lastAccessTime
    idleTime > HadoopConf.HDFS_ENABLE_CACHE_MAX_TIME || (System.currentTimeMillis() - this.lastAccessTime > HadoopConf.HDFS_ENABLE_CACHE_IDLE_TIME) && count <= 0
  }

}

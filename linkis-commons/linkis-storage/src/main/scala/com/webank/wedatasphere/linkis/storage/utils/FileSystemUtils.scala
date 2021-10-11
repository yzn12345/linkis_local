/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.storage.utils

import java.io.IOException
import java.util
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.fs.FileSystem
import com.webank.wedatasphere.linkis.storage.fs.impl.LocalFileSystem


object FileSystemUtils extends Logging{

  def copyFile(filePath: FsPath, origin: FsPath, user: String): Unit = {
    val fileSystem = FSFactory.getFsByProxyUser(filePath,user).asInstanceOf[FileSystem]
    Utils.tryFinally {
      fileSystem.init(null)
      if (!fileSystem.exists(filePath)) {
        if (!fileSystem.exists(filePath.getParent)) {
          fileSystem.mkdirs(filePath.getParent)
        }
        fileSystem.createNewFile(filePath)
      }
      fileSystem.copyFile(origin, filePath)
    }(Utils.tryQuietly(fileSystem.close()))
  }

  /**
    * Create a new file(创建新文件)
    * @param filePath
    * @param createParentWhenNotExists Whether to recursively create a directory(是否递归创建目录)
    */
  def createNewFile(filePath: FsPath, createParentWhenNotExists: Boolean): Unit = {
    val fileSystem = FSFactory.getFs(filePath).asInstanceOf[FileSystem]
    Utils.tryFinally {
      fileSystem.init(null)
      if (!fileSystem.exists(filePath)) {
        if (!fileSystem.exists(filePath.getParent)) {
          if(!createParentWhenNotExists) throw new IOException("parent dir " + filePath.getParent.getPath + " dose not exists.")
          fileSystem.mkdirs(filePath.getParent)
        }
        fileSystem.createNewFile(filePath)
      }
    }(Utils.tryQuietly(fileSystem.close()))
  }

  def createNewFile(filePath: FsPath, user:String,createParentWhenNotExists: Boolean): Unit = {
    val fileSystem = FSFactory.getFsByProxyUser(filePath,user).asInstanceOf[FileSystem]
    Utils.tryFinally {
      fileSystem.init(null)
      if (!fileSystem.exists(filePath)) {
        if (!fileSystem.exists(filePath.getParent)) {
          if(!createParentWhenNotExists) throw new IOException("parent dir " + filePath.getParent.getPath + " dose not exists.")
          mkdirs(fileSystem,filePath.getParent, user)
        }
        fileSystem.createNewFile(filePath)
        fileSystem match {
          case l:LocalFileSystem => fileSystem.setOwner(filePath,user)
          case _ => info(s"doesn't need to call setOwner")
        }
        /*fileSystem.setOwner(filePath,user,StorageConfiguration.STORAGE_HDFS_GROUP.getValue)*/
      }
    }(Utils.tryQuietly(fileSystem.close()))
  }

  /**
    * Recursively create a directory(递归创建目录)
    * @param fileSystem
    * @param dest
    * @param user
    * @throws
    * @return
    */
  @throws[IOException]
  def mkdirs(fileSystem: FileSystem,dest: FsPath, user: String): Boolean = {
    var parentPath = dest.getParent
    val dirsToMake = new util.Stack[FsPath]()
    dirsToMake.push(dest)
    while (!fileSystem.exists(parentPath)){
      dirsToMake.push(parentPath)
      parentPath = parentPath.getParent
    }
    if(! fileSystem.canExecute(parentPath)){
      throw new IOException("You have not permission to access path " + dest.getPath)
    }
    while (!dirsToMake.empty()){
      val path = dirsToMake.pop()
      fileSystem.mkdir(path)
      fileSystem match {
        case l:LocalFileSystem => fileSystem.setOwner(path,user)
        case _ => info(s"doesn't need to call setOwner")
      }
      //fileSystem.setOwner(path,user,StorageConfiguration.STORAGE_HDFS_GROUP.getValue)
    }
    true
  }

}

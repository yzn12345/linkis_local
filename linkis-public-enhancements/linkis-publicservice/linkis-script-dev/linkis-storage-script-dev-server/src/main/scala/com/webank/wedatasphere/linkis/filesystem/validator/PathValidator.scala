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

package com.webank.wedatasphere.linkis.filesystem.validator

import java.io.File

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.filesystem.conf.WorkSpaceConfiguration._
import com.webank.wedatasphere.linkis.filesystem.exception.WorkSpaceException
import com.webank.wedatasphere.linkis.filesystem.util.WorkspaceUtil
import com.webank.wedatasphere.linkis.server
import com.webank.wedatasphere.linkis.server.Message
import com.webank.wedatasphere.linkis.server.security.SecurityFilter
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.Response
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect, Pointcut}
import org.aspectj.lang.reflect.MethodSignature
import org.codehaus.jackson.JsonNode
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Aspect
@Component
class PathValidator extends Logging {

  @Pointcut("@annotation(javax.ws.rs.Path) && within(com.webank.wedatasphere.linkis.filesystem.restful.api.*)")
  def restfulResponseCatch(): Unit = {}

  def getPath(args: Array[Object], paramNames: Array[String]): String = {
    var path: String = null
    var index: Int = paramNames.indexOf("path")
    if (index != -1) {
      path = args(index).asInstanceOf[String]
    } else {
      index = paramNames.indexOf("json")
      if (index != -1) {
        args(index) match {
          case j: JsonNode if j.get("path") != null => path = j.get("path").getTextValue
          case m: java.util.Map[String, Object] if m.get("path") != null => path = m.get("path").asInstanceOf[String]
          case _ =>
        }
      }
    }
    path
  }

  def getUserName(args: Array[Object], paramNames: Array[String]): String = {
    var username: String = null
    paramNames.indexOf("req") match {
      case -1 =>
      case index: Int => {
        val proxyUser = paramNames.indexOf("proxyUser")
        if (proxyUser == -1 || StringUtils.isEmpty(args(proxyUser))) {
          username = SecurityFilter.getLoginUsername(args(index).asInstanceOf[HttpServletRequest])
        } else {
          //增加proxyuser的判断
          username = args(proxyUser).toString
        }
      }
    }
    username
  }

  def checkPath(path: String, username: String) = {
    //校验path的逻辑
    val userLocalRootPath: String = WorkspaceUtil.suffixTuning(LOCAL_USER_ROOT_PATH.getValue) +
      username
    var userHdfsRootPath: String = WorkspaceUtil.suffixTuning(HDFS_USER_ROOT_PATH_PREFIX.getValue) +
      username + HDFS_USER_ROOT_PATH_SUFFIX.getValue
    if (!(path.contains(StorageUtils.FILE_SCHEMA)) && !(path.contains(StorageUtils.HDFS_SCHEMA))) {
      throw new WorkSpaceException(80025, "the path should contain schema")
    }
    userHdfsRootPath = StringUtils.trimTrailingCharacter(userHdfsRootPath, File.separatorChar)
    if (path.contains("../")) {
      throw new WorkSpaceException(80026, "Relative path not allowed")
    }
    if (!(path.contains(userLocalRootPath)) && !(path.contains(userHdfsRootPath))) {
      throw new WorkSpaceException(80027, "The path needs to be within the user's own workspace path")
    }
  }

  def validate(args: Array[Object], paramNames: Array[String]) = {
    //获取path:String,json:JsonNode,json:Map中的path 参数
    val path: String = getPath(args, paramNames)
    val username: String = getUserName(args, paramNames)
    if (!StringUtils.isEmpty(path) && !StringUtils.isEmpty(username)) {
      logger.info(String.format("user:%s,path:%s", username, path))
      checkPath(path, username)
    }
  }

  @Around("restfulResponseCatch()")
  def dealResponseRestful(proceedingJoinPoint: ProceedingJoinPoint): Object = {
    val response: Response = server.catchIt {
      val signature = proceedingJoinPoint.getSignature.asInstanceOf[MethodSignature]
      logger.info("enter the path validator,the method is {}", signature.getName)
      if (FILESYSTEM_PATH_CHECK_TRIGGER.getValue) {
        logger.info("path check trigger is open,now check the path")
        validate(proceedingJoinPoint.getArgs, signature.getParameterNames)
      }
      Message.ok()
    }
    if (response.getStatus != 200) response else proceedingJoinPoint.proceed()
  }
}

object PathValidator {
  val validator = new PathValidator()

  def validate(path: String, username: String): Unit = {
    validator.checkPath(path, username)
  }
}

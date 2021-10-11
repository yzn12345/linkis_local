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

package com.webank.wedatasphere.linkis.entrance.interceptor.impl

import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor
import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtil
import org.slf4j.{Logger, LoggerFactory}

import java.lang
import java.util.regex.Pattern
import scala.util.matching.Regex


class CommentInterceptor extends EntranceInterceptor {
  /**
   * The apply function is to supplement the information of the incoming parameter task, making the content of this task more complete.
   *    * Additional information includes: database information supplement, custom variable substitution, code check, limit limit, etc.
   * apply函数是对传入参数task进行信息的补充，使得这个task的内容更加完整。
   * 补充的信息包括: 数据库信息补充、自定义变量替换、代码检查、limit限制等
   *
   * @param task
   * @param logAppender Used to cache the necessary reminder logs and pass them to the upper layer(用于缓存必要的提醒日志，传给上层)
   * @return
   */
  override def apply(task: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    val codeType = LabelUtil.getCodeType(task.getLabels)
    codeType match {
      case "sql" | "hql" | "psql" => task.setExecutionCode(SQLCommentHelper.dealComment(task.getExecutionCode))
      case "python" | "py" => task.setExecutionCode(PythonCommentHelper.dealComment(task.getExecutionCode))
      case "scala" | "java" => task.setExecutionCode(ScalaCommentHelper.dealComment(task.getExecutionCode))
      case "sh" | "shell" =>
      case _ =>
    }
    task
  }
}

trait CommentHelper {
  val commentPattern: Regex

  def dealComment(code: String): String
}

object SQLCommentHelper extends CommentHelper {
  override val commentPattern: Regex = """\s*--.+\s*""".r.unanchored
  private val comment = "(?ms)('(?:''|[^'])*')|--.*?$|/\\*.*?\\*/|#.*?$|"
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  override def dealComment(code: String): String = {
    try {
      val p = Pattern.compile(comment)
      val sql = p.matcher(code).replaceAll("$1")
      sql
    } catch {
      case e: Exception => logger.warn("sql comment failed")
        code
      case t: Throwable => logger.warn("sql comment failed")
        code
    }
  }
}

object PythonCommentHelper extends CommentHelper {
  override val commentPattern: Regex = """^\s*#.+\s*""".r.unanchored
  val pythonCommentPattern: String = "(?ms)([\"'](?:|[^'])*['\"])|#.*?$|/\\*.*?\\*/"

  override def dealComment(code: String): String = {
    code
  }
}


object ScalaCommentHelper extends CommentHelper {
  override val commentPattern: Regex = """^\s*//.+\s*""".r.unanchored
  private val scalaCommentPattern: String = "(?ms)([\"'](?:|[^'])*['\"])|//.*?$|/\\*.*?\\*/"

  override def dealComment(code: String): String = code
}


object CommentMain {
  def main(args: Array[String]): Unit = {
    val sqlCode = "select * from default.user;--你好;show tables"
    val sqlCode1 = "select * from default.user--你好;show tables"
    println(SQLCommentHelper.dealComment(sqlCode))
  }
}


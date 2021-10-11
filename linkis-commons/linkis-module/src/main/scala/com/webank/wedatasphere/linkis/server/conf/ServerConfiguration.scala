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

package com.webank.wedatasphere.linkis.server.conf

import java.io.File
import java.lang.Boolean

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, Configuration, TimeType}
import com.webank.wedatasphere.linkis.common.utils.{DESUtil, Logging, Utils}
import com.webank.wedatasphere.linkis.server.exception.BDPInitServerException
import org.apache.commons.lang.StringUtils
import sun.misc.BASE64Encoder


object ServerConfiguration extends Logging{
  val BDP_SERVER_EXCLUDE_PACKAGES = CommonVars("wds.linkis.server.component.exclude.packages", "")
  val BDP_SERVER_EXCLUDE_CLASSES = CommonVars("wds.linkis.server.component.exclude.classes", "")
  val BDP_SERVER_EXCLUDE_ANNOTATION = CommonVars("wds.linkis.server.component.exclude.annotation", "")
  val BDP_SERVER_SPRING_APPLICATION_LISTENERS = CommonVars("wds.linkis.server.spring.application.listeners", "")

  val BDP_SERVER_VERSION = CommonVars("wds.linkis.server.version", "").getValue
  if(StringUtils.isBlank(BDP_SERVER_VERSION)) {
    throw new BDPInitServerException(10010, "DataWorkCloud service must set the version, please add property [[wds.linkis.server.version]] to properties file.")
  }

  val cryptKey =  new BASE64Encoder().encode(CommonVars("wds.linkis.crypt.key", "bdp-for-server").getValue.getBytes)
  private val ticketHeader = CommonVars("wds.linkis.ticket.header", "bfs_").getValue
  def getUsernameByTicket(ticketId: String): Option[String] = if(StringUtils.isEmpty(ticketId)) None
  else {
    val userName = DESUtil.decrypt(ticketId, ServerConfiguration.cryptKey)
    if(userName.startsWith(ticketHeader)) Some(userName.substring(ticketHeader.length))
    else None
  }
  def getUsernameByTicket(ticketId: Any): Option[String] = if(ticketId == null) None else getUsernameByTicket(ticketId.toString)
  def getTicketByUsername(userName: String): String = {
      DESUtil.encrypt(ticketHeader + userName, ServerConfiguration.cryptKey)
  }

  val BDP_TEST_USER = CommonVars("wds.linkis.test.user", "")

  val BDP_SERVER_HOME = CommonVars("wds.linkis.server.home", CommonVars("LINKIS_HOME", "").getValue)
  val BDP_SERVER_DISTINCT_MODE = CommonVars("wds.linkis.server.distinct.mode", new Boolean(true))
  if(!BDP_SERVER_DISTINCT_MODE.getValue && StringUtils.isEmpty(BDP_SERVER_HOME.getValue))
    throw new BDPInitServerException(11000, "wds.linkis.server.home或BDP_SERVER_HOME haven't set!")
  val BDP_SERVER_SOCKET_MODE = CommonVars("wds.linkis.server.socket.mode", new Boolean(false))
  val BDP_SERVER_IDENT_STRING = CommonVars("wds.linkis.server.ident.string", "true")
  val BDP_SERVER_SERVER_JETTY_NAME = CommonVars("wds.linkis.server.jetty.name", "")
  val BDP_SERVER_ADDRESS = CommonVars("wds.linkis.server.address", Utils.getLocalHostname)
  val BDP_SERVER_PORT = CommonVars("wds.linkis.server.port", 20303)
  val BDP_SERVER_SECURITY_FILTER = CommonVars("wds.linkis.server.security.filter", "com.webank.wedatasphere.linkis.server.security.SecurityFilter")
  val BDP_SERVER_SECURITY_REFERER_VALIDATE = CommonVars("wds.linkis.server.security.referer.validate", false)
  val BDP_SERVER_SECURITY_SSL = CommonVars("wds.linkis.server.security.ssl", false)
  val BDP_SERVER_SECURITY_SSL_EXCLUDE_PROTOCOLS = CommonVars("wds.linkis.server.security.ssl.excludeProtocols", "SSLv2,SSLv3")
  val BDP_SERVER_SECURITY_SSL_KEYSTORE_PATH = CommonVars("wds.linkis.server.security.ssl.keystore.path",
    new File(BDP_SERVER_HOME.getValue, "keystore").getPath)
  val BDP_SERVER_SECURITY_SSL_KEYSTORE_TYPE = CommonVars("wds.linkis.server.security.ssl.keystore.type", "JKS")
  val BDP_SERVER_SECURITY_SSL_KEYSTORE_PASSWORD = CommonVars("wds.linkis.server.security.ssl.keystore.password", "")
  val BDP_SERVER_SECURITY_SSL_KEY_MANAGER_PASSWORD = CommonVars("wds.linkis.server.security.ssl.key.manager.password", "")
  val BDP_SERVER_SECURITY_SSL_CIPHER_SUITES = CommonVars("wds.linkis.server.security.ssl.cipher.suites",
    "")

  val BDP_SERVER_SERVER_CONTEXT_PATH = CommonVars("wds.linkis.server.context.path", "/")
  val BDP_SERVER_RESTFUL_URI = CommonVars("wds.linkis.server.restful.uri", "/api/rest_j/" + BDP_SERVER_VERSION)
  val BDP_SERVER_USER_URI = CommonVars("wds.linkis.server.user.restful.uri", "/api/rest_j/" + BDP_SERVER_VERSION + "/user")
  val BDP_SERVER_RESTFUL_LOGIN_URI = CommonVars("wds.linkis.server.user.restful.login.uri", new File(BDP_SERVER_USER_URI.getValue, "login").getPath)
  val BDP_SERVER_SECURITY_SSL_URI = CommonVars("wds.linkis.server.user.security.ssl.uri", new File(BDP_SERVER_USER_URI.getValue, "publicKey").getPath)
  val BDP_SERVER_SOCKET_URI = CommonVars("wds.linkis.server.socket.uri", "/ws")
  val BDP_SERVER_SOCKET_LOGIN_URI = CommonVars("wds.linkis.server.socket.login.uri", "/ws/user/login")
  val BDP_SERVER_WAR = CommonVars("wds.linkis.server.war", new File(BDP_SERVER_HOME.getValue, "web/dist").getPath)
  val BDP_SERVER_WAR_TEMPDIR = CommonVars("wds.linkis.server.war.tempdir", new File(BDP_SERVER_HOME.getValue, "web/webapps").getPath)
  val BDP_SERVER_SERVER_DEFAULT_DIR_ALLOWED = CommonVars("wds.linkis.server.default.dir.allowed", "false")
  val BDP_SERVER_WEB_SESSION_TIMEOUT = CommonVars("wds.linkis.server.web.session.timeout", new TimeType("2h"))

  val BDP_SERVER_EVENT_QUEUE_SIZE = CommonVars("wds.linkis.server.event.queue.size", 5000)
  val BDP_SERVER_EVENT_CONSUMER_THREAD_SIZE = CommonVars("wds.linkis.server.event.consumer.thread", 10)
  val BDP_SERVER_EVENT_CONSUMER_THREAD_FREE_MAX = CommonVars("wds.linkis.server.event.consumer.thread.max.free", new TimeType("2m"))

  val BDP_SERVER_SOCKET_QUEUE_SIZE = CommonVars("wds.linkis.server.socket.queue.size", BDP_SERVER_EVENT_CONSUMER_THREAD_SIZE.getValue * 20)
  val BDP_SERVER_SOCKET_TEXT_MESSAGE_SIZE_MAX = CommonVars("wds.linkis.server.socket.text.message.size.max", "1024000")

  val BDP_SERVER_ENCODING = Configuration.BDP_ENCODING

  val BDP_SERVER_RESTFUL_SCAN_PACKAGES = CommonVars("wds.linkis.server.restful.scan.packages", "")
  val BDP_SERVER_RESTFUL_REGISTER_CLASSES = CommonVars("wds.linkis.server.restful.register.classes", "")
//  val BDP_SERVER_SOCKET_SERVICE_SCAN_PACKAGES = CommonVars("wds.linkis.server.socket.service.scan.packages", BDP_SERVER_RESTFUL_SCAN_PACKAGES.getValue)
  val IS_GATEWAY = CommonVars("wds.linkis.is.gateway", "false")
}

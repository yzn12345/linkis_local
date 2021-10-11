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

package com.webank.wedatasphere.linkis.httpclient.authentication

import java.util.concurrent.ConcurrentHashMap

import com.webank.wedatasphere.linkis.httpclient.Client
import com.webank.wedatasphere.linkis.httpclient.config.ClientConfig
import com.webank.wedatasphere.linkis.httpclient.request.{Action, UserAction}
import org.apache.commons.lang.StringUtils
import org.apache.http.HttpResponse


abstract class AbstractAuthenticationStrategy extends AuthenticationStrategy {
  private var client: Client = _
  private val userNameToAuthentications = new ConcurrentHashMap[String, Authentication]()
  private var clientConfig: ClientConfig = _
  protected val sessionMaxAliveTime: Long

  def setClient(client: Client): Unit = this.client = client
  def getClient: Client = client

  protected def getKeyByUserAndURL(user: String, serverUrl: String): String = user + "@" + serverUrl

  protected def getUser(requestAction: Action): String = requestAction match {
    case _: AuthenticationAction => null
    case authAction: UserAction => authAction.getUser
    case _ if StringUtils.isNotBlank(clientConfig.getAuthTokenKey) => clientConfig.getAuthTokenKey
    case _ => null
  }

  protected def getKey(requestAction: Action, serverUrl: String): String = {
    val user = getUser(requestAction)
    if(user == null) return null
    getKeyByUserAndURL(user, serverUrl)
  }

  def setClientConfig(clientConfig: ClientConfig): Unit = this.clientConfig = clientConfig
  def getClientConfig: ClientConfig = clientConfig

  def login(requestAction: Action, serverUrl: String): Authentication = {
    val key = getKey(requestAction, serverUrl)
    if(key == null) return null
    if(userNameToAuthentications.containsKey(key) && !isTimeout(userNameToAuthentications.get(key))) {
      val authenticationAction = userNameToAuthentications.get(key)
      authenticationAction.updateLastAccessTime()
      authenticationAction
    } else key.intern() synchronized {
      var authentication = userNameToAuthentications.get(key)
      if(authentication == null || isTimeout(authentication)) {
        authentication = tryLogin(requestAction, serverUrl)
        userNameToAuthentications.put(key, authentication)
      }
      authentication
    }
  }

  def tryLogin(requestAction: Action, serverUrl: String): Authentication = {
    val action = getAuthenticationAction(requestAction, serverUrl)
    client.execute(action, 5000) match {
      case r: AuthenticationResult => r.getAuthentication
    }
  }

  protected def getAuthenticationAction(requestAction: Action, serverUrl: String): AuthenticationAction

  def getAuthenticationResult(response: HttpResponse, requestAction: AuthenticationAction): AuthenticationResult

  def isTimeout(authentication: Authentication): Boolean = System.currentTimeMillis() - authentication.getLastAccessTime >= sessionMaxAliveTime
}
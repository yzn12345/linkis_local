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

package com.webank.wedatasphere.linkis.httpclient.dws.response

import java.util

import com.webank.wedatasphere.linkis.httpclient.authentication.{Authentication, AuthenticationResult, HttpAuthentication}
import com.webank.wedatasphere.linkis.httpclient.exception.HttpMessageParseException
import org.apache.http.HttpResponse
import org.apache.http.cookie.Cookie
import org.apache.http.util.EntityUtils

class DWSAuthenticationResult(response: HttpResponse, serverUrl: String) extends AuthenticationResult with DWSResult {

  setResponse()

  private def setResponse(): Unit = {
    val entity = response.getEntity
    val responseBody: String = if (entity != null) {
      EntityUtils.toString(entity, "UTF-8")
    } else {
      null
    }
    val statusCode: Int = response.getStatusLine.getStatusCode
    val url: String = serverUrl
    val contentType: String = entity.getContentType.getValue
    set(responseBody, statusCode, url, contentType)
  }


  override def getAuthentication: Authentication = if (getStatus == 0) new HttpAuthentication {
    private var lastAccessTime: Long = System.currentTimeMillis

    override def authToCookies: Array[Cookie] = Array.empty

    override def authToHeaders: util.Map[String, String] = new util.HashMap[String, String]()

    override def authToMap: util.Map[String, String] = new util.HashMap[String, String]()

    override def getLastAccessTime: Long = lastAccessTime

    override def updateLastAccessTime(): Unit = lastAccessTime = System.currentTimeMillis
  } else throw new HttpMessageParseException(s"login to gateway $serverUrl failed! Reason: " + getMessage)


}

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


package com.webank.wedatasphere.linkis.httpclient.dws

import java.util

import com.webank.wedatasphere.linkis.common.io.{Fs, FsPath}
import com.webank.wedatasphere.linkis.common.utils.{JsonUtils, Logging}
import com.webank.wedatasphere.linkis.httpclient.AbstractHttpClient
import com.webank.wedatasphere.linkis.httpclient.discovery.Discovery
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfig
import com.webank.wedatasphere.linkis.httpclient.dws.discovery.DWSGatewayDiscovery
import com.webank.wedatasphere.linkis.httpclient.dws.request.DWSHttpAction
import com.webank.wedatasphere.linkis.httpclient.dws.response.{DWSHttpMessageFactory, DWSHttpMessageResultInfo, DWSResult}
import com.webank.wedatasphere.linkis.httpclient.request.HttpAction
import com.webank.wedatasphere.linkis.httpclient.response.impl.DefaultHttpResult
import com.webank.wedatasphere.linkis.httpclient.response.{HttpResult, ListResult, Result}
import org.apache.commons.beanutils.BeanUtils
import org.apache.commons.lang.{ClassUtils, StringUtils}
import org.apache.http.{HttpException, HttpResponse}

import scala.collection.JavaConversions

class DWSHttpClient(clientConfig: DWSClientConfig, clientName: String)
  extends AbstractHttpClient(clientConfig, clientName) with  Logging{

  override protected def createDiscovery(): Discovery = new DWSGatewayDiscovery


  override protected def prepareAction(requestAction: HttpAction): HttpAction = {
    requestAction match {
      case dwsAction: DWSHttpAction => dwsAction.setDWSVersion(clientConfig.getDWSVersion)
      case _ =>
    }
    requestAction
  }

  override protected def httpResponseToResult(response: HttpResponse, requestAction: HttpAction, responseBody: String): Option[Result] = {
    val entity = response.getEntity
    val statusCode: Int = response.getStatusLine.getStatusCode
    val url: String = requestAction.getURL

    if (null == entity.getContentType && statusCode == 200) {
      info("response is null, return success Result")
      return Some(Result())
    }
    val contentType: String = entity.getContentType.getValue
    DWSHttpMessageFactory.getDWSHttpMessageResult(url).map { case DWSHttpMessageResultInfo(_, clazz) =>
      clazz match {
        case c if ClassUtils.isAssignable(c, classOf[DWSResult]) =>
          val dwsResult = clazz.getConstructor().newInstance().asInstanceOf[DWSResult]
          dwsResult.set(responseBody, statusCode, url, contentType)
          BeanUtils.populate(dwsResult, dwsResult.getData)
          return Some(dwsResult)
        case _ =>
      }

      def transfer(value: Result, map: util.Map[String, Object]): Unit = {
        value match {
          case httpResult: HttpResult =>
            httpResult.set(responseBody, statusCode, url, contentType)
          case _ =>
        }
        BeanUtils.populate(value, map)
        fillResultFields(map, value)
      }
      deserializeResponseBody(responseBody) match {
        case map: util.Map[String, Object] =>
          val value = clazz.getConstructor().newInstance().asInstanceOf[Result]
          transfer(value, map)
          value
        case list: util.List[util.Map[String, Object]] =>
          val results = JavaConversions.asScalaBuffer(list).map { map =>
            val value = clazz.getConstructor().newInstance().asInstanceOf[Result]
            transfer(value, map)
            value
          }.toArray
          new ListResult(responseBody, results)
      }
    }.orElse(nonDWSResponseToResult(response, requestAction, responseBody))
  }

  protected def deserializeResponseBody(responseBody: String): Any = {
    if (responseBody.startsWith("{") && responseBody.endsWith("}"))
      DWSHttpClient.jacksonJson.readValue(responseBody, classOf[util.Map[String, Object]])
    else if (responseBody.startsWith("[") && responseBody.endsWith("]"))
      DWSHttpClient.jacksonJson.readValue(responseBody, classOf[util.List[util.Map[String, Object]]])
    else if (StringUtils.isEmpty(responseBody)) new util.HashMap[String, Object]
    else if (responseBody.length > 200) throw new HttpException(responseBody.substring(0, 200))
    else throw new HttpException(responseBody)
  }

  protected def nonDWSResponseToResult(response: HttpResponse, requestAction: HttpAction, responseBody: String): Option[Result] = {
    val httpResult = new DefaultHttpResult
    httpResult.set(responseBody, response.getStatusLine.getStatusCode, requestAction.getURL, response.getEntity.getContentType.getValue)
    Some(httpResult)
  }

  protected def fillResultFields(responseMap: util.Map[String, Object], value: Result): Unit = {}

  //TODO Consistent with workspace, plus expiration time(与workspace保持一致，加上过期时间)
  //  override protected def getFsByUser(user: String, path: FsPath): Fs = FSFactory.getFsByProxyUser(path, user)
  override protected def getFsByUser(user: String, path: FsPath): Fs = {
    null
  }
}
object DWSHttpClient {
  val jacksonJson = JsonUtils.jackson
}
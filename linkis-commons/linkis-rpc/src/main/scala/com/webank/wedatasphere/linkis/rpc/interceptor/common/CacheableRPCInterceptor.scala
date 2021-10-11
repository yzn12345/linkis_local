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

package com.webank.wedatasphere.linkis.rpc.interceptor.common

import java.util.concurrent.{Callable, TimeUnit}

import com.google.common.cache.{Cache, CacheBuilder, RemovalListener, RemovalNotification}
import com.webank.wedatasphere.linkis.common.exception.WarnException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.protocol.CacheableProtocol
import com.webank.wedatasphere.linkis.rpc.interceptor.{RPCInterceptor, RPCInterceptorChain, RPCInterceptorExchange}
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

import scala.tools.scalap.scalax.util.StringUtil

@Component
class CacheableRPCInterceptor extends RPCInterceptor with Logging{

  private val guavaCache: Cache[Any, Any] = CacheBuilder.newBuilder().concurrencyLevel(5)
    .expireAfterAccess(120000, TimeUnit.MILLISECONDS).initialCapacity(20)  //TODO Make parameters(做成参数)
    .maximumSize(1000).recordStats().removalListener(new RemovalListener[Any, Any] {
    override def onRemoval(removalNotification: RemovalNotification[Any, Any]): Unit = {
      debug(s"CacheSender removed key => ${removalNotification.getKey}, value => ${removalNotification.getValue}.")
    }
  }).asInstanceOf[CacheBuilder[Any, Any]].build()

  override val order: Int = 10

  override def intercept(interceptorExchange: RPCInterceptorExchange, chain: RPCInterceptorChain): Any = interceptorExchange.getProtocol match {
    case cacheable: CacheableProtocol =>
      guavaCache.get(cacheable.toString, new Callable[Any] {
        override def call(): Any = {
          val returnMsg = chain.handle(interceptorExchange)
          returnMsg match {
           case warn: WarnException =>
              throw warn
            case _ =>
              returnMsg
          }
        }
      })
    case _ => chain.handle(interceptorExchange)
  }

  def removeCache(cacheKey: String): Boolean ={
      Utils.tryCatch{
        if(!StringUtils.isEmpty(cacheKey)){
          guavaCache.invalidate(cacheKey)
        }
        true
      }{
        case exception: Exception => {
          warn(s"Failed to clean RPC cache, cache key:${cacheKey}", exception)
          false
        }
      }
    }

}

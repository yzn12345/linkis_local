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

package com.webank.wedatasphere.linkis.rpc.transform

import java.lang.reflect.{ParameterizedType, Type}
import java.util

import com.webank.wedatasphere.linkis.DataWorkCloudApplication
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol
import com.webank.wedatasphere.linkis.rpc.errorcode.RPCErrorConstants
import com.webank.wedatasphere.linkis.rpc.exception.DWCURIException
import com.webank.wedatasphere.linkis.rpc.serializer.ProtostuffSerializeUtil
import com.webank.wedatasphere.linkis.server.{EXCEPTION_MSG, Message}
import org.apache.commons.lang.ClassUtils
import org.json4s.{DefaultFormats, Formats, Serializer}

import scala.collection.JavaConversions


private[linkis] trait RPCProduct {

  def toMessage(t: Any): Message

  def notFound(): Message

  def ok(): Message

}
private[linkis] object RPCProduct extends Logging {

  private[rpc] val IS_REQUEST_PROTOCOL_CLASS = "rpc_is_request_protocol"
  private[rpc] val IS_SCALA_CLASS = "rpc_is_scala_class"
  private[rpc] val CLASS_VALUE = "rpc_object_class"
  private[rpc] val OBJECT_VALUE = "rpc_object_value"
  private[rpc] implicit var formats: Formats = DefaultFormats + JavaCollectionSerializer + JavaMapSerializer
  private var serializerClasses: List[Class[_]] = List.empty
  private val rpcProduct: RPCProduct = new RPCProduct {
    private val rpcFormats = DataWorkCloudApplication.getApplicationContext.getBeansOfType(classOf[RPCFormats])
    if (rpcFormats != null && !rpcFormats.isEmpty) {
      val serializers = JavaConversions.mapAsScalaMap(rpcFormats).map(_._2.getSerializers).toArray.flatMap(_.iterator)
      setFormats(serializers)
    }
    override def toMessage(t: Any): Message = {
      if (t == null) throw new DWCURIException(10001, "The transmitted bean is Null.(传输的bean为Null.)")
      val message = Message.ok("RPC Message.")
      if (isRequestProtocol(t)) {
        message.data(IS_REQUEST_PROTOCOL_CLASS, "true")
      } else {
        message.data(IS_REQUEST_PROTOCOL_CLASS, "false")
      }
        message.data(OBJECT_VALUE, ProtostuffSerializeUtil.serialize(t))
      /*} else if (isScalaClass(t)) {
        message.data(IS_SCALA_CLASS, "true")
        message.data(OBJECT_VALUE, Serialization.write(t.asInstanceOf[AnyRef]))
      } else {
        message.data(IS_SCALA_CLASS, "false")
        message.data(OBJECT_VALUE, BDPJettyServerHelper.gson.toJson(t))
      }*/
      message.setMethod("/rpc/message")
      message.data(CLASS_VALUE, t.getClass.getName)
    }

    override def notFound(): Message = {
      val message = Message.error("RPC Message.")
      message.setMethod("/rpc/message")
      message.data(EXCEPTION_MSG, new DWCURIException(RPCErrorConstants.URL_ERROR, "The service does not " +
        "exist for the available Receiver.(服务不存在可用的Receiver.)").toMap)
    }

    override def ok(): Message = {
      val message = Message.ok("RPC Message.")
      message.setMethod("/rpc/message")
      message
    }
  }
  private[rpc] def setFormats(serializer: Array[Serializer[_]]): Unit ={
    this.formats = (serializer :+ JavaCollectionSerializer :+ JavaMapSerializer).foldLeft(DefaultFormats.asInstanceOf[Formats])(_ + _)
    serializerClasses = formats.customSerializers.map(s => getActualTypeClass(s.getClass.getGenericSuperclass))
      .filter(_ != null) ++: List(classOf[util.List[_]], classOf[util.Map[_, _]])
    info("RPC Serializers: " + this.formats.customSerializers.map(_.getClass.getSimpleName) + ", serializerClasses: " +
      "" + serializerClasses)
  }

  private def getActualTypeClass(classType: Type): Class[_] = classType match {
    case p: ParameterizedType =>
      val params = p.getActualTypeArguments
      if (params == null || params.isEmpty) null
      else getActualTypeClass(params(0))
    case c: Class[_] => c
    case _ => null
  }

  private[rpc] def isRequestProtocol(obj: Any): Boolean = obj.isInstanceOf[RequestProtocol]

  private[rpc] def isScalaClass(obj: Any): Boolean =
    (obj.isInstanceOf[Product] && obj.isInstanceOf[Serializable]) ||
      serializerClasses.exists(ClassUtils.isAssignable(obj.getClass, _)) ||
      obj.getClass.getName.startsWith("scala.")

  private[rpc] def getSerializableScalaClass(clazz: Class[_]): Class[_] =
    serializerClasses.find(ClassUtils.isAssignable(clazz, _)).getOrElse(clazz)

  def getRPCProduct: RPCProduct = rpcProduct
}
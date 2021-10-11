/*
 *
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.linkis.manager.common.serializer

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.resource.{CommonNodeResource, ResourceSerializer}
import com.webank.wedatasphere.linkis.manager.common.protocol.em.RegisterEMRequest
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, DefaultFormats, Extraction}


object RegisterEMRequestSerializer extends CustomSerializer[RegisterEMRequest](implicit formats => ( {
  case JObject(List(("serviceInstance", serviceInstance),
  ("labels", labels),
  ("nodeResource", nodeResource),
  ("user", user),
  ("alias", alias))) =>
    val registerEMRequest = new RegisterEMRequest
    registerEMRequest.setServiceInstance(serviceInstance.extract[ServiceInstance])
    registerEMRequest.setAlias(alias.extract[String])
    //registerEMRequest.setLabels(labels.extract[java.util.HashMap[String, Object]])
    registerEMRequest.setUser(user.extract[String])
    registerEMRequest.setNodeResource(nodeResource.extract[CommonNodeResource])
    registerEMRequest
}, {
  case c: RegisterEMRequest =>
    implicit val formats = DefaultFormats + ResourceSerializer + NodeResourceSerializer
    ("serviceInstance", Extraction.decompose(c.getServiceInstance)) ~
      ("labels", Extraction.decompose(c.getLabels)) ~
      ("nodeResource", Extraction.decompose(c.getNodeResource)) ~
      ("user", c.getUser) ~
      ("alias", c.getAlias)
}))

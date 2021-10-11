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

package com.webank.wedatasphere.linkis.resourcemanager

import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, Extraction}


trait ResultResource

case class NotEnoughResource(val reason: String = null) extends ResultResource

case class AvailableResource(val ticketId: String) extends ResultResource

object ResultResourceSerializer extends CustomSerializer[ResultResource](implicit formats => ( {
  case JObject(List(("NotEnoughResource", JObject(List(("reason", reason)))))) => NotEnoughResource(reason.extract[String])
  case JObject(List(("AvailableResource", JObject(List(("ticketId", ticketId)))))) => AvailableResource(ticketId.extract[String])
}, {
  case r: NotEnoughResource => ("NotEnoughResource", ("reason", Extraction.decompose(r.reason)))
  case r: AvailableResource => ("AvailableResource", ("ticketId", Extraction.decompose(r.ticketId)))
}))
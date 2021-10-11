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

import com.webank.wedatasphere.linkis.manager.common.entity.resource.ResourceSerializer
import com.webank.wedatasphere.linkis.rpc.transform.RPCFormats
import org.json4s.Serializer



//@Component
class RMRPCFormats extends RPCFormats {

  override def getSerializers: Array[Serializer[_]] = Array(ResultResourceSerializer, ResourceSerializer)
}

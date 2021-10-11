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

package com.webank.wedatasphere.linkis.datasourcemanager.common.protocol

import java.util

/**
 * Store error code map
 */
trait DsmQueryProtocol {

}

/**
 * Query request of Data Source Information
 * @param id
 */
case class DsInfoQueryRequest(id: String, system: String) extends DsmQueryProtocol

/**
 * Response of parameter map
 * @param params
 */
case class DsInfoResponse(status: Boolean, dsType: String, params : util.Map[String, Object], creator: String) extends DsmQueryProtocol


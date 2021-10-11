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

package com.webank.wedatasphere.linkis.configuration.validate

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper

class JsonValidator extends Validator{

  override def validate(value: String, range: String): Boolean = {
    Utils.tryCatch{
      BDPJettyServerHelper.gson.fromJson(value,classOf[java.util.HashMap[String,String]])
      true
    }{
      case _ => false
    }
  }

  override var kind: String = "Json"
}
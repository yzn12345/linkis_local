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

import com.webank.wedatasphere.linkis.common.utils.Logging
import javax.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class ValidatorManagerImpl extends ValidatorManager with Logging{

  private var validators: Array[Validator] = _

  @PostConstruct
  def init() = {
    validators = Array(new NumericalValidator,new OneOfValidator,new FloatValidator,new NoneValidator,new RegexValidator)
  }

  override def getOrCreateValidator(kind: String): Validator = {
    info(s"find a validator $kind")
    validators.find(_.kind.equalsIgnoreCase(kind)).get
    // TODO:  If it is custom, create a validator class with reflection(如果是自定义的，就用反射创建一个validator类)
  }
}

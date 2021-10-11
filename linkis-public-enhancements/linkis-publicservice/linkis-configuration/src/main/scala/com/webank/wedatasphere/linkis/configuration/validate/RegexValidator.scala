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

import org.apache.commons.lang.StringUtils

class RegexValidator extends Validator{
  override var kind: String = "Regex"

  override def validate(value: String, range: String): Boolean = {
    value matches(range.r.regex)
  }
}

object RegexValidator {
  def main (args: Array[String] ): Unit = {
   print(StringUtils.isEmpty(""))
}
}

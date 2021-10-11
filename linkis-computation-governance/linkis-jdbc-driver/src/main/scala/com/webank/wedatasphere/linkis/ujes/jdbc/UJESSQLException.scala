/*
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
 */
package com.webank.wedatasphere.linkis.ujes.jdbc

import com.webank.wedatasphere.linkis.common.exception.ErrorException


class UJESSQLException (errorCode: UJESSQLErrorCode) extends ErrorException(errorCode.getCode,errorCode.getMsg) {
  def this(errorCode: UJESSQLErrorCode, msg: String) {
    this(errorCode)
    setErrCode(errorCode.getCode)
    setDesc(msg)
  }


  /**
    * add to deal with errorinfo derived from jobInfo
    * @param errorCode
    * @param msg
    */
  def this(errorCode: Int,msg: String) {
    this(UJESSQLErrorCode.ERRORINFO_FROM_JOBINFO)
    setDesc(msg)
    setErrCode(errorCode)
  }

}

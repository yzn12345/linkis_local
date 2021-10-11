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

package com.webank.wedatasphere.linkis.common.log

import java.text.SimpleDateFormat
import java.util.Date



object LogUtils {


  private def getTimeFormat:String = {
    val simpleDateFormat:SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm")
    val now = new Date(System.currentTimeMillis())
    simpleDateFormat.format(now)
    //now.toString(ISODateTimeFormat.yearMonthDay()) + " " + now.toString(ISODateTimeFormat.hourMinuteSecondMillis())
  }

  def generateInfo(rawLog:String):String = {
    getTimeFormat + " " + "INFO" + " " + rawLog
  }

  def generateERROR(rawLog:String):String = {
    getTimeFormat + " " + "ERROR" + " " + rawLog
  }

  def generateWarn(rawLog:String):String = {
    getTimeFormat + " " + "WARN" + " " + rawLog
  }

  def generateSystemInfo(rawLog:String):String = {
    getTimeFormat + " " + "SYSTEM-INFO" + " " + rawLog
  }

  def generateSystemError(rawLog:String):String = {
    getTimeFormat + " " + "SYSTEM-ERROR" + " " + rawLog
  }

  def generateSystemWarn(rawLog:String):String = {
    getTimeFormat + " " + "SYSTEM-WARN" + " " + rawLog
  }


}

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

package com.webank.wedatasphere.linkis.common.conf

import java.util.Date
import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.utils.ByteTimeUtils

import scala.concurrent.duration.Duration


class TimeType(timeStr: String) {
  def this(ms: Long) = this(ByteTimeUtils.msDurationToString(ms))
  val toLong = ByteTimeUtils.timeStringAsMs(timeStr)
  val toDate = new Date(toLong)
  val toDuration = Duration(toLong, TimeUnit.MILLISECONDS)
  override val toString = timeStr
}
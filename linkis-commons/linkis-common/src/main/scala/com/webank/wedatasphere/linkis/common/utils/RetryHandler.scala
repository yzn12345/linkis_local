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

package com.webank.wedatasphere.linkis.common.utils

import com.webank.wedatasphere.linkis.common.exception.FatalException
import org.apache.commons.lang.{ClassUtils => CommonClassUtils}

import scala.collection.mutable.ArrayBuffer


trait RetryHandler extends Logging {

  private var retryNum = 2
  private var period = 100l
  private var maxPeriod = 1000l
  private val retryExceptions = ArrayBuffer[Class[_  <: Throwable]]()

  def setRetryNum(retryNum: Int): Unit = this.retryNum = retryNum
  def getRetryNum: Int = retryNum
  def setRetryPeriod(retryPeriod: Long): Unit = this.period = retryPeriod
  def getRetryPeriod: Long = period
  def setRetryMaxPeriod(retryMaxPeriod: Long): Unit = this.maxPeriod = retryMaxPeriod
  def getRetryMaxPeriod: Long = maxPeriod
  def addRetryException(t: Class[_  <: Throwable]): Unit = retryExceptions += t
  def getRetryExceptions = retryExceptions.toArray

  def exceptionCanRetry(t: Throwable): Boolean = !t.isInstanceOf[FatalException] &&
    retryExceptions.exists(c => CommonClassUtils.isAssignable(t.getClass, c))

  def nextInterval(attempt: Int): Long = {
    val interval = (this.period.toDouble * Math.pow(1.5D, (attempt - 1).toDouble)).toLong
    if (interval > this.maxPeriod) this.maxPeriod
    else interval
  }

  def retry[T](op: => T, retryName: String): T = {
    if(retryExceptions.isEmpty || retryNum <= 1) return op
    var retry = 0
    var result = null.asInstanceOf[T]
    while(retry < retryNum && result == null) result = Utils.tryCatch(op) { t =>
      retry += 1
      if(retry >= retryNum) throw t
      else if(exceptionCanRetry(t)) {
        val retryInterval = nextInterval(retry)
        info(retryName + s" failed with ${t.getClass.getName}, wait ${ByteTimeUtils.msDurationToString(retryInterval)} for next retry. Retried $retry++ ...")
        Utils.tryQuietly(Thread.sleep(retryInterval))
        null.asInstanceOf[T]
      } else throw t
    }
    result
  }
}

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

package com.webank.wedatasphere.linkis.ujes.client.response

import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult

@DWSHttpMessageResult("/api/rest_j/v\\d+/entrance/(\\S+)/status")
class JobStatusResult extends UJESJobResult with Status {

  override def getJobStatus: String = getData.get("status").asInstanceOf[String]

}
trait Status {

  def getJobStatus: String

  def isSucceed: Boolean = getJobStatus == "Succeed"

  def isFailed: Boolean = getJobStatus match {
    case "Failed" | "Cancelled" | "Timeout" => true
    case _ => false
  }

  def isRunning: Boolean = getJobStatus == "Running"

  def isWaitForRetry: Boolean = getJobStatus == "WaitForRetry"

  def isScheduled: Boolean = getJobStatus == "Scheduled"

  def isCompleted: Boolean = isSucceed || isFailed
}
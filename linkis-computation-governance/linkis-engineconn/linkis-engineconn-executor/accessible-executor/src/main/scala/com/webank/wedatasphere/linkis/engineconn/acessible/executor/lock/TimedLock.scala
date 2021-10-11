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

package com.webank.wedatasphere.linkis.engineconn.acessible.executor.lock

import com.webank.wedatasphere.linkis.engineconn.acessible.executor.entity.AccessibleExecutor

trait TimedLock {

  @throws[Exception]
  def acquire(executor: AccessibleExecutor): Unit

  def tryAcquire(executor: AccessibleExecutor): Boolean

  @throws[Exception]
  def release(): Unit

  @throws[Exception]
  def forceRelease(): Unit

  def numOfPending(): Int

  def isAcquired(): Boolean

  def isExpired(): Boolean

  @throws[Exception]
  def renew(): Boolean

  def resetTimeout(timeout: Long): Unit
}

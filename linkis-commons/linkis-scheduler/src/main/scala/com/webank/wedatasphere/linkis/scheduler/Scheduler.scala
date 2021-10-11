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

package com.webank.wedatasphere.linkis.scheduler

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEvent
import com.webank.wedatasphere.linkis.scheduler.queue.fifoqueue.FIFOScheduler
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelScheduler


abstract class Scheduler {
  def init(): Unit
  def start(): Unit
  def getName: String
  def submit(event: SchedulerEvent): Unit
  def get(event: SchedulerEvent): Option[SchedulerEvent]
  def get(eventId: String): Option[SchedulerEvent]
  def shutdown(): Unit
  def getSchedulerContext: SchedulerContext
}

object Scheduler extends Logging{
  def createScheduler(scheduleType: String, schedulerContext: SchedulerContext): Option[Scheduler]={
    scheduleType match {
      case "FIFO" => Some(new FIFOScheduler(schedulerContext))
      case "PARA" => Some(new ParallelScheduler(schedulerContext))
      case _ => {
        error("Please enter the correct scheduling type!(请输入正确的调度类型!)")
        None
      }
    }
  }
}
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

package com.webank.wedatasphere.linkis.scheduler.queue.fifoqueue


import java.util.concurrent.{ExecutorService, ThreadPoolExecutor}

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext
import com.webank.wedatasphere.linkis.scheduler.exception.SchedulerErrorException
import com.webank.wedatasphere.linkis.scheduler.listener.ConsumerListener
import com.webank.wedatasphere.linkis.scheduler.queue.{Consumer, ConsumerManager, Group, LoopArrayQueue}


class FIFOConsumerManager(groupName: String) extends ConsumerManager {

  def this() = this(FIFOGroupFactory.FIFO_GROUP_NAME)

  private var group: Group = _
  private var executorService: ThreadPoolExecutor = _
  private var consumerListener: ConsumerListener = _
  private var consumerQueue: LoopArrayQueue = _
  private var consumer: Consumer = _


  override def setSchedulerContext(schedulerContext: SchedulerContext): Unit = {
    super.setSchedulerContext(schedulerContext)
    group = getSchedulerContext.getOrCreateGroupFactory.getOrCreateGroup(null)
    executorService = group match {
      case g: FIFOGroup => Utils.newCachedThreadPool(g.getMaxRunningJobs + 2, groupName + "-Thread-")
      case _ => throw new SchedulerErrorException(13000, s"FIFOConsumerManager need a FIFOGroup, but ${group.getClass} is supported.")
    }
    consumerQueue = new LoopArrayQueue(getSchedulerContext.getOrCreateGroupFactory.getOrCreateGroup(null))
    consumer = createConsumer(groupName)
  }

  override def setConsumerListener(consumerListener: ConsumerListener): Unit = this.consumerListener = consumerListener

  override def getOrCreateExecutorService: ExecutorService = executorService

  override def getOrCreateConsumer(groupName: String): Consumer = consumer

  override protected def createConsumer(groupName: String): Consumer = {
    val group = getSchedulerContext.getOrCreateGroupFactory.getOrCreateGroup(null)
    val consumer = new FIFOUserConsumer(getSchedulerContext, getOrCreateExecutorService, group)
    consumer.setGroup(group)
    consumer.setConsumeQueue(consumerQueue)
    if(consumerListener != null) consumerListener.onConsumerCreated(consumer)
    consumer.start()
    consumer
  }

  override def destroyConsumer(groupName: String): Unit = {
    //ignore
  }

  override def shutdown(): Unit = {
    if(consumerListener != null) consumerListener.onConsumerDestroyed(consumer)
    consumer.shutdown()
    executorService.shutdownNow()
  }

  override def listConsumers(): Array[Consumer] = Array(consumer)
}

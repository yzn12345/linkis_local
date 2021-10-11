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

package com.webank.wedatasphere.linkis.engineconn.acessible.executor.conf

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, TimeType}

object AccessibleExecutorConfiguration {

  val ENGINECONN_LOG_CACHE_NUM = CommonVars("wds.linkis.engineconn.log.cache.default", 500)


  val ENGINECONN_IGNORE_WORDS = CommonVars("wds.linkis.engineconn.ignore.words", "org.apache.spark.deploy.yarn.Client")

  val ENGINECONN_PASS_WORDS = CommonVars("wds.linkis.engineconn.pass.words", "org.apache.hadoop.hive.ql.exec.Task")

  val ENGINECONN_LOG_NUM_SEND_ONCE = CommonVars("wds.linkis.engineconn.log.send.once", 100)

  val ENGINECONN_LOG_SEND_TIME_INTERVAL = CommonVars("wds.linkis.engineconn.log.send.time.interval", 30)

  val ENGINECONN_LOG_SEND_SIZE = CommonVars[Int]("wds.linkis.engineconn.log.send.cache.size", 5)


  val ENGINECONN_MAX_FREE_TIME = CommonVars("wds.linkis.engineconn.max.free.time", new TimeType("1h"))

  val ENGINECONN_LOCK_CHECK_INTERVAL = CommonVars("wds.linkis.engineconn.lock.free.interval", new TimeType("3m"))


  val ENGINECONN_SUPPORT_PARALLELISM = CommonVars("wds.linkis.engineconn.support.parallelism", false)


  val ENGINECONN_HEARTBEAT_TIME = CommonVars("wds.linkis.engineconn.heartbeat.time", new TimeType("3m"))

}

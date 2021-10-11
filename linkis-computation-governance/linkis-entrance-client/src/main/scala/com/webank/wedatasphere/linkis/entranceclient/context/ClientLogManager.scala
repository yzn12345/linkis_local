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

package com.webank.wedatasphere.linkis.entranceclient.context

import java.io.{InputStream, OutputStream}

import com.webank.wedatasphere.linkis.entrance.log._
import com.webank.wedatasphere.linkis.scheduler.queue.Job
import com.webank.wedatasphere.linkis.server.conf.ServerConfiguration
import org.apache.commons.io.input.NullInputStream
import org.apache.commons.io.output.NullOutputStream

class ClientLogManager extends CacheLogManager {
  override def getLogReader(execId: String): LogReader = {
    new CacheLogReader("", ServerConfiguration.BDP_SERVER_ENCODING.getValue,
      new Cache(10), "") {
      override def getInputStream: InputStream = new NullInputStream(0)
    }
  }

  override def createLogWriter(job: Job): LogWriter = {
    new NullCacheLogWriter(ServerConfiguration.BDP_SERVER_ENCODING.getValue,
      new Cache(20))
  }
  class NullCacheLogWriter(charset:String,
                           sharedCache:Cache,
                           override protected val outputStream: OutputStream = new NullOutputStream)
    extends CacheLogWriter("", charset, sharedCache, "")
}

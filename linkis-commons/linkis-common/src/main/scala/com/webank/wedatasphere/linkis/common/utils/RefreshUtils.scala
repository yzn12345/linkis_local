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

import java.io.File
import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.conf.Configuration
import org.apache.commons.io.FileUtils


object RefreshUtils {

  def registerFileRefresh(period: Long, file: String, deal: java.util.List[String] => Unit): Unit = {
    Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      val f = new File(file)
      var fileModifiedTime = if(f.exists()) f.lastModified() else 0
      override def run(): Unit = {
        if(!f.exists()) return
        if(f.lastModified() > fileModifiedTime) {
          deal(FileUtils.readLines(f, Configuration.BDP_ENCODING.getValue))
          fileModifiedTime = f.lastModified()
        }
      }
    }, period, period, TimeUnit.MILLISECONDS)
  }

}
abstract class Deal {
  def deal(line: String): Unit
}

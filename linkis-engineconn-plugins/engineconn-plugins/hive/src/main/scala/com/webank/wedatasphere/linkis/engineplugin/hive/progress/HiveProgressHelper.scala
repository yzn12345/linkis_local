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

package com.webank.wedatasphere.linkis.engineplugin.hive.progress

import java.util.concurrent.LinkedBlockingQueue

import com.webank.wedatasphere.linkis.engineplugin.hive.log.HiveProgress
import org.slf4j.{Logger, LoggerFactory}

object HiveProgressHelper {

  private val hiveProgressQueue: LinkedBlockingQueue[HiveProgress] = new LinkedBlockingQueue[HiveProgress]()

  private var appid: String = _

  private val logger:Logger = LoggerFactory.getLogger(getClass)

  private var singleSQLProgress:Float = 0.0f

  def storeSingleSQLProgress(singleSQLProgress:Float):Unit = this.singleSQLProgress = singleSQLProgress

  def getSingleSQLProgress:Float = this.singleSQLProgress

  def clearSingleSQLProgress():Unit = this.singleSQLProgress = 0.0f


  def storeHiveProgress(hiveProgress:java.util.List[HiveProgress]):Unit = {
    //logger.info("begin to store hive progress")
    import scala.collection.JavaConversions._
    hiveProgress foreach hiveProgressQueue.put
  }


  def clearHiveProgress():Unit = {
    hiveProgressQueue.synchronized{
      hiveProgressQueue.clear()
    }
    clearAppid()
  }


  def getHiveProgress:LinkedBlockingQueue[HiveProgress] = hiveProgressQueue

  def storeAppid(appid:String):Unit = this.appid = appid

  def getAppid:String = this.appid

  def clearAppid():Unit = this.appid = null



}

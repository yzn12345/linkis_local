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

package com.webank.wedatasphere.linkis.manager.engineplugin.common.creation


import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.{DefaultEngineConn, EngineConn}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.exception.EngineConnBuildFailedException
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineConnModeLabel
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineType.EngineType

import scala.collection.JavaConversions.asScalaBuffer


trait EngineConnFactory {

  def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn

}

trait AbstractEngineConnFactory extends EngineConnFactory {

  protected def getEngineConnType: EngineType

  protected def createEngineConnSession(engineCreationContext: EngineCreationContext): Any

  override def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn = {
    val engineConn = new DefaultEngineConn(engineCreationContext)
    val engineConnSession = createEngineConnSession(engineCreationContext)
    engineConn.setEngineConnType(getEngineConnType.toString)
    engineConn.setEngineConnSession(engineConnSession)
    engineConn
  }
}

/**
  * For only one kind of executor, like hive, python ...
  */
trait SingleExecutorEngineConnFactory extends AbstractEngineConnFactory with ExecutorFactory

trait SingleLabelExecutorEngineConnFactory extends SingleExecutorEngineConnFactory with LabelExecutorFactory

/**
  * For many kinds of executor, such as spark with spark-sql and spark-shell and pyspark
  */
trait MultiExecutorEngineConnFactory extends AbstractEngineConnFactory with Logging {


  def getExecutorFactories: Array[ExecutorFactory]

  def getDefaultExecutorFactory: ExecutorFactory =
    getExecutorFactories.find(_.getClass == getDefaultExecutorFactoryClass)
      .getOrElse(throw new EngineConnBuildFailedException(20000, "Cannot find default ExecutorFactory."))

  protected def getDefaultExecutorFactoryClass: Class[_ <: ExecutorFactory]

  protected def getEngineConnModeLabel(labels: util.List[Label[_]]): EngineConnModeLabel =
    labels.find(_.isInstanceOf[EngineConnModeLabel]).map(_.asInstanceOf[EngineConnModeLabel]).orNull

}
/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.manager.am.label

import java.util

import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.em.EMInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import com.webank.wedatasphere.linkis.manager.service.common.label.LabelChecker
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._


@Component
class AMLabelChecker extends LabelChecker {

  override def checkEngineLabel(labelList: util.List[Label[_]]): Boolean = {
    checkCorrespondingLabel(labelList, classOf[EngineTypeLabel], classOf[UserCreatorLabel])
  }

  override def checkEMLabel(labelList: util.List[Label[_]]): Boolean = {
    checkCorrespondingLabel(labelList, classOf[EMInstanceLabel])
  }

  override def checkCorrespondingLabel(labelList: util.List[Label[_]], clazz: Class[_]*): Boolean = {
    // TODO: 是否需要做子类的判断
    labelList.filter(null != _).map(_.getClass).containsAll(clazz)
  }
}

object AD{
  def main(args: Array[String]): Unit = {
    val label = new UserCreatorLabel
    val checker = new AMLabelChecker
    println(checker.checkCorrespondingLabel(util.Arrays.asList(label),classOf[UserCreatorLabel]))
  }
}

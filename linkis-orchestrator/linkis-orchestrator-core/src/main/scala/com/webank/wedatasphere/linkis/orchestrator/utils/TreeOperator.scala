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

package com.webank.wedatasphere.linkis.orchestrator.utils

import com.webank.wedatasphere.linkis.orchestrator.domain.TreeNode

/**
  *
  *
  */
object TreeOperator {

  def mapChildren[TreeType <: TreeNode[TreeType], NewType <: TreeType](node: TreeType, f: TreeType => NewType): TreeType = {
    node.withNewChildren(node.getChildren.map(f).asInstanceOf[Array[TreeType]])
    node
  }

  def transformChildren[TreeType <: TreeNode[TreeType]](node: TreeType, func: PartialFunction[TreeType, TreeType]): TreeType = {
    val afterTransform = func.applyOrElse(this.asInstanceOf[TreeType], identity[TreeType])
    /*if (this eq afterTransform) {
      mapChildren(node,transformChildren(node, func))
    } else {
      afterTransform.mapChildren(_.transformChildren(func))
    }*/
    node
  }

}

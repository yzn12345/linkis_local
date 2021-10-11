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

package com.webank.wedatasphere.linkis.manager.common.protocol.bml

import java.util.Date


trait LocalResource extends BmlResource {

  def setIsPublicPath(isPublicPath:Boolean):Unit

  def getIsPublicPath:Boolean

  def setPath(path:String):Unit

  def getPath:String

  def setDownloadTime(date:Date):Unit

  def getDownloadTime:Date

}

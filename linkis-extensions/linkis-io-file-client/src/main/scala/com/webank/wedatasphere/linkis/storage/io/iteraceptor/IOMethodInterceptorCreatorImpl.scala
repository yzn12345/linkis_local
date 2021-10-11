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

package com.webank.wedatasphere.linkis.storage.io.iteraceptor

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.storage.io.client.IOClient
import com.webank.wedatasphere.linkis.storage.io.IOMethodInterceptorCreator
import com.webank.wedatasphere.linkis.storage.io.client.IOClient
import javax.annotation.PostConstruct
import net.sf.cglib.proxy.MethodInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component("ioMethod")
class IOMethodInterceptorCreatorImpl extends IOMethodInterceptorCreator with Logging {

  @Autowired
  private var ioClient: IOClient = _

  @PostConstruct
  def init(): Unit = {
    info("IOMethodInterceptorCreatorImpl finished init")
    IOMethodInterceptorCreator.register(this)
  }

  override def createIOMethodInterceptor(fsName: String): MethodInterceptor = {
    val ioMethodInterceptor = new IOMethodInterceptor(fsName)
    ioMethodInterceptor.setIoClient(ioClient)
    ioMethodInterceptor
  }

}

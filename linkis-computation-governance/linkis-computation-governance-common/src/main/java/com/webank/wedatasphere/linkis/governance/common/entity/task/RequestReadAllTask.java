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

package com.webank.wedatasphere.linkis.governance.common.entity.task;

import com.webank.wedatasphere.linkis.protocol.query.QueryProtocol;

public class RequestReadAllTask implements QueryProtocol {
    /**
     * Instance of microservices, through this example, we can take all the tasks below this instance from the database
     * instance 微服务的实例，通过这个实例，我们可以将这个实例下面的所有的task全部从数据库中拿出来
     */
    private String instance;

    public RequestReadAllTask(String instance) {
        super();
        this.instance = instance;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }


}

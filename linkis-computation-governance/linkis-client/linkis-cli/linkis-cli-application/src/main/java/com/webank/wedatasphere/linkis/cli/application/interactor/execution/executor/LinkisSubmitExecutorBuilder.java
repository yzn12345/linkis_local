/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cli.application.interactor.execution.executor;

import com.webank.wedatasphere.linkis.cli.application.driver.LinkisClientDriver;
import com.webank.wedatasphere.linkis.cli.application.driver.UjesClientDriverBuilder;
import com.webank.wedatasphere.linkis.cli.application.driver.transformer.UjesClientDriverTransformer;
import com.webank.wedatasphere.linkis.cli.common.entity.execution.executor.Executor;
import com.webank.wedatasphere.linkis.cli.core.interactor.execution.executor.ExecutorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LinkisSubmitExecutorBuilder extends ExecutorBuilder {
    private static Logger logger = LoggerFactory.getLogger(LinkisSubmitExecutorBuilder.class);


    @Override
    public Executor build() {
    /*
      build ujes client
     */
        LinkisClientDriver driver = new UjesClientDriverBuilder() //can be reused
                .setStdVarAccess(stdVarAccess)
                .setSysVarAccess(sysVarAccess)
                .build();

        ((LinkisSubmitExecutor) targetObj).setDriver(driver);
        ((LinkisSubmitExecutor) targetObj).setDriverTransformer(new UjesClientDriverTransformer());

        return super.build();
    }

    @Override
    protected LinkisSubmitExecutor getTargetNewInstance() {
        return new LinkisSubmitExecutor();
    }

}
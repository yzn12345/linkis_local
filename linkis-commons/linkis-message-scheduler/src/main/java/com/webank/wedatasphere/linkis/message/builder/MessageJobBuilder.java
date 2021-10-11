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

package com.webank.wedatasphere.linkis.message.builder;

import com.webank.wedatasphere.linkis.message.context.AbstractMessageSchedulerContext;
import com.webank.wedatasphere.linkis.message.scheduler.MethodExecuteWrapper;
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol;

import java.util.List;
import java.util.Map;


public interface MessageJobBuilder {

    MessageJobBuilder of();

    MessageJobBuilder with(RequestProtocol requestProtocol);

    MessageJobBuilder with(Map<String, List<MethodExecuteWrapper>> methodExecuteWrappers);

    MessageJobBuilder with(ServiceMethodContext smc);

    MessageJobBuilder with(AbstractMessageSchedulerContext context);

    MessageJob build();
}

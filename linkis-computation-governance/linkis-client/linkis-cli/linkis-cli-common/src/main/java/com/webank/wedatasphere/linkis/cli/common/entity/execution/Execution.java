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

package com.webank.wedatasphere.linkis.cli.common.entity.execution;

import com.webank.wedatasphere.linkis.cli.common.entity.execution.executor.Executor;
import com.webank.wedatasphere.linkis.cli.common.entity.job.Job;


public interface Execution {
    ExecutionResult execute(Executor executor, Job job); //start execution

    boolean terminate(Executor executor, Job job);                                 //terminate execution(often used in shutdown hook)
}


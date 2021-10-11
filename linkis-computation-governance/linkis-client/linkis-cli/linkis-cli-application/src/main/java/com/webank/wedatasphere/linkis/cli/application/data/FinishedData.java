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

package com.webank.wedatasphere.linkis.cli.application.data;

import com.webank.wedatasphere.linkis.cli.common.entity.execution.ExecutionResult;
import com.webank.wedatasphere.linkis.cli.common.entity.result.ResultHandler;

public class FinishedData {
    ExecutionResult executionResult;
    ResultHandler[] resultHandlers;

    public FinishedData(ExecutionResult executionResult, ResultHandler[] resultHandlers) {
        this.executionResult = executionResult;
        this.resultHandlers = resultHandlers;
    }

    public ExecutionResult getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(ExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    public ResultHandler[] getResultHandlers() {
        return resultHandlers;
    }

    public void setResultHandlers(ResultHandler[] resultHandlers) {
        this.resultHandlers = resultHandlers;
    }
}
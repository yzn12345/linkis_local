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

package com.webank.wedatasphere.linkis.governance.common.exception.engineconn;

/**
 * ErrorCode of Engine start with 40000
 * <p>
 */
public class EngineConnExecutorErrorCode {

    public static final int INVALID_ENGINE_TYPE = 40100;

    public static final int INVALID_METHOD = 40101;

    public static final int INVALID_PARAMS = 40102;

    public static final int INVALID_LOCK = 40103;

    public static final int INVALID_TASK = 40104;

    public static final int SEND_TO_ENTRANCE_ERROR = 40105;

    public static final int INIT_EXECUTOR_FAILED = 40106;

}

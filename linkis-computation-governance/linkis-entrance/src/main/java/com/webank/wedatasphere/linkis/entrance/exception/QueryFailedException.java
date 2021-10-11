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

package com.webank.wedatasphere.linkis.entrance.exception;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;

/**
 * Description: entrance module to query submit database query possible exceptions
 * Description: entrance 模块向 query 提交数据库查询可能出现的异常
 */
public class QueryFailedException extends ErrorException {
    public QueryFailedException(int errCode, String desc){
        super(errCode, desc);
    }
    public QueryFailedException(int errCode, String desc, Exception e){
        super(errCode, desc);
        this.initCause(e);
    }
}

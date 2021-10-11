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


package com.webank.wedatasphere.linkis.protocol.query.cache;

import com.webank.wedatasphere.linkis.protocol.query.QueryProtocol;

import java.util.List;

public class RequestReadCache implements QueryProtocol {
    private String executionContent;
    private String user;
    private Long readCacheBefore;
    private List<String> labelsStr;

    public RequestReadCache(String executionContent, String user, List<String> labelsStr, Long readCacheBefore) {
        this.executionContent = executionContent;
        this.user = user;
        this.labelsStr = labelsStr;
        this.readCacheBefore = readCacheBefore;
    }

    public String getExecutionContent() {
        return executionContent;
    }

    public String getUser() {
        return user;
    }

    public Long getReadCacheBefore() {
        return readCacheBefore;
    }

    public List<String> getLabelsStr() {
        return labelsStr;
    }

}

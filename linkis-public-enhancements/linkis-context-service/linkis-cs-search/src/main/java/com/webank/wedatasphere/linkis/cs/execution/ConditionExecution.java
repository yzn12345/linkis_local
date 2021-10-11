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

package com.webank.wedatasphere.linkis.cs.execution;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.execution.fetcher.ContextCacheFetcher;
import com.webank.wedatasphere.linkis.cs.execution.matcher.ContextSearchMatcher;
import com.webank.wedatasphere.linkis.cs.execution.ruler.ContextSearchRuler;

import java.util.List;

public interface ConditionExecution {

    ContextSearchMatcher getContextSearchMatcher();
    ContextSearchRuler getContextSearchRuler();
    ContextCacheFetcher getContextCacheFetcher();
    List<ContextKeyValue> execute();
    void setContextCacheService(ContextCacheService contextCacheService);
    ContextCacheService getContextCacheService();
    Condition getCondition();

}

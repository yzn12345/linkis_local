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

package com.webank.wedatasphere.linkis.cs.execution.fetcher;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.execution.ruler.ContextSearchRuler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class IterateContextCacheFetcher extends AbstractContextCacheFetcher{

    private static final Logger logger = LoggerFactory.getLogger(IterateContextCacheFetcher.class);

    ContextSearchRuler contextSearchRuler;

    public IterateContextCacheFetcher(ContextCacheService contextCacheService, ContextSearchRuler contextSearchRuler) {
        super(contextCacheService);
        this.contextSearchRuler = contextSearchRuler;
    }

    private IterateContextCacheFetcher(ContextCacheService contextCacheService) {
        super(contextCacheService);
    }

    @Override
    public List<ContextKeyValue> fetch(ContextID contextID) {
        return contextSearchRuler.rule(contextCacheService.getAll(contextID));
    }


}

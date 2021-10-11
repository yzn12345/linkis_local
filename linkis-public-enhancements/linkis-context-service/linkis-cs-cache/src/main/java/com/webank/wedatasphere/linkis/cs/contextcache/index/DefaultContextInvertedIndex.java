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

package com.webank.wedatasphere.linkis.cs.contextcache.index;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultContextInvertedIndex implements ContextInvertedIndex {

    /**
     *  TODO Added ContextKey scoring feature
     */
    Multimap<String, String> indexMultimap = HashMultimap.create();

    @Override
    public List<String> getContextKeys(String keyword) {
        Collection<String> keywords = indexMultimap.get(keyword);
        return new ArrayList<String>(keywords);
    }

    @Override
    public boolean addValue(String keyword, String contextKey) {
       return indexMultimap.put(keyword, contextKey);
    }

    @Override
    public List<String> getContextKeys(List<String> keywords) {
        if(CollectionUtils.isEmpty(keywords)) {
            return null;
        }
        List<String> contextKeys = new ArrayList<>();
        for(String keyword:keywords){
            contextKeys.addAll(getContextKeys(keyword));
        }
        return contextKeys;
    }

    @Override
    public boolean remove(String keyword, String contextKey) {
        return indexMultimap.remove(keyword, contextKey);
    }
}

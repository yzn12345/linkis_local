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

package com.webank.wedatasphere.linkis.cs.condition.construction;

import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.AndCondition;

import java.util.Map;

public class AndConditionParser implements ConditionParser{
    @Override
    public Condition parse(Map<Object, Object> conditionMap) {
        Map<Object, Object> left = (Map<Object, Object>) conditionMap.get("left");
        Map<Object, Object> right = (Map<Object, Object>) conditionMap.get("right");
        return new AndCondition(
            parserMap.get(left.get("type")).parse(left),
            parserMap.get(right.get("type")).parse(right)
        );
    }

    @Override
    public String getName() {
        return "And";
    }
}

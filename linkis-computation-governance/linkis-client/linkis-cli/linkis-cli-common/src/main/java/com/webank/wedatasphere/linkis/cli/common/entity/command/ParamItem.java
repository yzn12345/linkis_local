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

package com.webank.wedatasphere.linkis.cli.common.entity.command;

/**
 * @description: param value and default value
 */
public class ParamItem {
    private String keyPrefix;
    private String key;
    private Object value;
    private Object defaultValue;
    private boolean hasVal;


    public ParamItem(String keyPrefix, String key, Object value, boolean hasVal, Object defaultValue) {
        this.keyPrefix = keyPrefix;
        this.key = key;
        this.value = value;
        this.defaultValue = defaultValue;
        this.hasVal = hasVal;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean hasVal() {
        return this.hasVal;
    }

    @Override
    public String toString() {
        return "ParamItem{" +
                "keyPrefix='" + keyPrefix + '\'' +
                "key='" + key + '\'' +
                ", value=" + value +
                ", defaultValue=" + defaultValue +
                '}';
    }
}
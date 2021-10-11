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

package com.webank.wedatasphere.linkis.metadata.type;


public enum Lifecycle {
    /**
     *
     */
    Permanent("永久"),
    Todday("当天有效"),
    ThisWeek("一周有效"),
    ThisMonth("一月有效"),
    HalfYear("半年有效");
    private String name;

    Lifecycle(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

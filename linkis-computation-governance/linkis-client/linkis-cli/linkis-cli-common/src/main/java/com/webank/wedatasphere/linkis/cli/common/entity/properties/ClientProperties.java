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

package com.webank.wedatasphere.linkis.cli.common.entity.properties;

import java.util.HashMap;

/**
 * @description: configurations/system variables in the form of kv-pairs
 */
public class ClientProperties extends HashMap<Object, Object> {
    /**
     * propsId identifies which source this Map belongs to
     */
    String propsId;
    String propertiesSourcePath;

    public String getPropsId() {
        return propsId;
    }

    public void setPropsId(String propsId) {
        this.propsId = propsId;
    }

    public String getPropertiesSourcePath() {
        return propertiesSourcePath;
    }

    public void setPropertiesSourcePath(String propertiesSourcePath) {
        this.propertiesSourcePath = propertiesSourcePath;
    }


}
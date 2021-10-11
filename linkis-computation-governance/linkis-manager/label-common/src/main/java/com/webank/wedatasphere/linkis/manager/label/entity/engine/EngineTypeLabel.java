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

package com.webank.wedatasphere.linkis.manager.label.entity.engine;

import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant;
import com.webank.wedatasphere.linkis.manager.label.entity.*;
import com.webank.wedatasphere.linkis.manager.label.entity.annon.ValueSerialNum;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

public class EngineTypeLabel extends GenericLabel implements EngineNodeLabel, EMNodeLabel {

    public EngineTypeLabel() {
        setLabelKey(LabelKeyConstant.ENGINE_TYPE_KEY);
    }

    @Override
    public Feature getFeature() {
        return Feature.CORE;
    }

    public String getEngineType() {
        if (null == getValue()) {
            return null;
        }
        return getValue().get("engineType");
    }

    public String getVersion() {
        if (null == getValue()) {
            return null;
        }

        return getValue().get("version");
    }

    @ValueSerialNum(0)
    public void setEngineType(String type) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("engineType", type);
    }

    @ValueSerialNum(1)
    public void setVersion(String version) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("version", version);
    }

    @Override
    public Boolean isEmpty() {
        return StringUtils.isBlank(getEngineType()) || StringUtils.isBlank(getVersion());
    }
}

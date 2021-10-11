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

package com.webank.wedatasphere.linkis.manager.label.builder;

import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactory;
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import com.webank.wedatasphere.linkis.manager.label.constant.LabelConstant;
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant;
import com.webank.wedatasphere.linkis.manager.label.entity.CombinedLabelImpl;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;
import com.webank.wedatasphere.linkis.manager.label.exception.LabelErrorException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CombinedLabelBuilder implements LabelBuilder {

    private static LabelBuilderFactory labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory();

    @Override
    public boolean canBuild(String labelKey) {
        if (labelKey.startsWith(LabelKeyConstant.COMBINED_LABEL_KEY_PREFIX)) {
            return true;
        }
        return false;
    }

    @Override
    public Label<?> build(String labelKey, @Nullable Object valueObj) throws LabelErrorException {
        if (null != valueObj || valueObj instanceof List) {
            try {
                List<Label<?>> labels = (List<Label<?>>) valueObj;
                return new CombinedLabelImpl(labels);
            } catch (Throwable e) {
                throw new LabelErrorException(LabelConstant.LABEL_BUILDER_ERROR_CODE, "Failed to build combinedLabel", e);
            }
        }
        return null;
    }

    public Label<?> buildFromStringValue(String labelKey, String stringValue) throws LabelErrorException {
        String[] keyList = labelKey.replace(LabelKeyConstant.COMBINED_LABEL_KEY_PREFIX,"").split("_");
        String[] valueList = stringValue.split(",");
        ArrayList<Label> labels = new ArrayList<>();
        for(int i = 0; i < keyList.length; i++){
            Label label = labelFactory.createLabel(keyList[i], valueList[i]);
            labels.add(label);
        }
        Label newLabel = build(labelKey, labels);
        return newLabel;
    }
}

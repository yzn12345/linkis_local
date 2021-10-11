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

package com.webank.wedatasphere.linkis.cli.core.interactor.command.template.option;

import com.webank.wedatasphere.linkis.cli.core.utils.converter.PredefinedStringConverters;
import org.apache.commons.lang3.StringUtils;

/**
 * @description: Flag is a special type of {@link StdOption}. Its only accepts boolean value.
 */
public class Flag extends StdOption<Boolean> implements Cloneable {
    public Flag(final String keyPrefix, final String key, final String[] paramNames, final String description, final boolean isOptional, final boolean defaultValue) {
        super(keyPrefix, key, paramNames, description, isOptional, defaultValue, PredefinedStringConverters.BOOLEAN_CONVERTER);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\t").append(StringUtils.join(paramNames, "|")).append(" <").append(
                this.getDefaultValue().getClass().getSimpleName()).append(">").append(System.lineSeparator());

        sb.append("\t\t").append(this.getDefaultValue()).append(System.lineSeparator());

        sb.append("\t\tdefault by: ")
                .append(this.getDefaultValue())
                .append(System.lineSeparator());

        sb.append("\t\toptional:").append(isOptional());

        return sb.toString();
    }

    @Override
    public Flag clone() throws CloneNotSupportedException {
        return (Flag) super.clone();
    }
}

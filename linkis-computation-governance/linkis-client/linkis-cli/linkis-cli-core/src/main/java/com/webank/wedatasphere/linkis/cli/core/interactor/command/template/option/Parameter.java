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

import com.webank.wedatasphere.linkis.cli.common.utils.converter.AbstractStringConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * @description: Data Structure for command Parameter. Command String does not contain the name of Parameter.
 */
public class Parameter<T> extends BaseOption<T> implements Cloneable {
    final String paramName;

    public Parameter(final String keyPrefix, final String key, final String paramName, final String description, final boolean isOptional,
                     final AbstractStringConverter<T> converter, final T defaultValue) {
        super(keyPrefix, key, description, isOptional, defaultValue, converter);
        this.paramName = paramName;
    }

    @Override
    public String getParamName() {
        return paramName;
    }

    public String repr() {
        String temp = accepctArrayValue() ? paramName + " ... " : paramName;
        return isOptional() ? "[" + temp + "]" : "<" + temp + ">";
    }

    @Override
    public String toString() {

        T defaultValue = this.getDefaultValue();
        String description = this.getDescription();

        StringBuilder sb = new StringBuilder();
        sb.append("\t").append(paramName).append(" <").append(
                defaultValue.getClass().getSimpleName()).append(">").append(System.lineSeparator());

        sb.append("\t\t").append(description).append(System.lineSeparator());

        sb.append("\t\tdefault by: ")
                .append(
                        defaultValue.getClass().isArray() ? StringUtils.join((Object[]) defaultValue, ", ") :
                                (defaultValue == null ? "" : defaultValue.toString())
                )
                .append(System.lineSeparator());

        sb.append("\t\toptional:").append(isOptional());

        return sb.toString();
    }

    public boolean accepctArrayValue() {
        T defaultValue = this.getDefaultValue();
        return defaultValue.getClass().isArray();
    }

    @Override
    public Parameter<T> clone() throws CloneNotSupportedException {
        return (Parameter<T>) super.clone();
    }
}

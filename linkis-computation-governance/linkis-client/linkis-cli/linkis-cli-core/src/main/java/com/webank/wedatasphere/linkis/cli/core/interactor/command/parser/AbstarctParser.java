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

package com.webank.wedatasphere.linkis.cli.core.interactor.command.parser;

import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdOption;
import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdTemplate;
import com.webank.wedatasphere.linkis.cli.common.entity.command.ParamItem;
import com.webank.wedatasphere.linkis.cli.common.entity.command.Params;
import com.webank.wedatasphere.linkis.cli.common.exception.error.ErrorLevel;
import com.webank.wedatasphere.linkis.cli.core.exception.CommandException;
import com.webank.wedatasphere.linkis.cli.core.exception.TransformerException;
import com.webank.wedatasphere.linkis.cli.core.exception.error.CommonErrMsg;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.fitter.Fitter;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.parser.result.ParseResult;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.parser.transformer.ParamKeyMapper;
import com.webank.wedatasphere.linkis.cli.core.utils.SpecialMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @description: Given {@link CmdTemplate}, Parse user input arguements into {@link Params}
 */
public abstract class AbstarctParser implements Parser {
    private static final Logger logger = LoggerFactory.getLogger(AbstarctParser.class);

    Fitter fitter;
    CmdTemplate template;
    ParamKeyMapper mapper;

    public AbstarctParser setFitter(Fitter fitter) {
        this.fitter = fitter;
        return this;
    }

    public AbstarctParser setTemplate(CmdTemplate template) {
        this.template = template;
        return this;
    }

    public AbstarctParser setMapper(ParamKeyMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public void checkInit() {
        if (fitter == null) {
            throw new CommandException("CMD0013", ErrorLevel.ERROR, CommonErrMsg.ParserInitErr, "failed to init parser: \n" + "fitter is null");
        }
        if (template == null) {
            throw new CommandException("CMD0013", ErrorLevel.ERROR, CommonErrMsg.ParserInitErr, "failed to init parser: \n" + "template is null");
        }
    }


    public Params templateToParams(CmdTemplate template, ParamKeyMapper mapper) {
        List<CmdOption<?>> options = template.getOptions();

        Map<String, ParamItem> params = new HashMap<>();
        StringBuilder mapperInfoSb = new StringBuilder();

        for (CmdOption<?> option : options) {
            ParamItem paramItem = optionToParamItem(option, params, mapper, mapperInfoSb);
            if (params.containsKey(paramItem.getKey())) {
                throw new TransformerException("TFM0012", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                        MessageFormat.format("Failed to convert option into ParamItem: params contains duplicated identifier: \"{0}\"", option.getKey()));

            } else {
                params.put(paramItem.getKey(), paramItem);
            }
        }

        if (mapper != null) {
            logger.info("\nParam Key Substitution: " + mapperInfoSb.toString());
        }
        Map<String, Object> extraProperties = new HashMap<>();
        return new Params(null, template.getCmdType(), params, extraProperties);
    }

    protected ParamItem optionToParamItem(CmdOption<?> option, Map<String, ParamItem> params, ParamKeyMapper mapper, StringBuilder mapperInfoSb) {
        String oriKey = option.getKey();
        String keyPrefix = option.getKeyPrefix();
        String key = oriKey;
        if (params.containsKey(oriKey)) {
            throw new TransformerException("TFM0012", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                    MessageFormat.format("Failed to convert option into ParamItem: params contains duplicated identifier: \"{0}\"", option.getKey()));
        }
        if (mapper != null) {
            key = getMappedKey(oriKey, mapper, mapperInfoSb);
        }
        Object val = option.getValue();
        if (option.getValue() != null &&
                option.getValue() instanceof Map &&
                !(option.getValue() instanceof SpecialMap)) {
            Map<String, Object> subMap;
            try {
                subMap = (Map<String, Object>) option.getValue();
            } catch (Exception e) {
                logger.warn("Failed to get subMap for option: " + option.getKey() + ".", e);
                return null;
            }
            if (mapper != null) {
                subMap = mapper.getMappedMapping(subMap);
            }
            val = addPrefixToSubMapKey(subMap, keyPrefix);
        }
        return new ParamItem(keyPrefix, key, val, option.hasVal(), option.getDefaultValue());
    }

    private Map<String, Object> addPrefixToSubMapKey(Map<String, Object> subMap, String keyPrefix) {
        Map<String, Object> newSubMap = new HashMap<>();
        StringBuilder keyBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : subMap.entrySet()) {
            if (StringUtils.isNotBlank(keyPrefix) &&
                    !StringUtils.startsWith(entry.getKey(), keyPrefix)) {
                keyBuilder.append(keyPrefix).append('.').append(entry.getKey());
            } else {
                keyBuilder.append(entry.getKey());
            }
            newSubMap.put(keyBuilder.toString(), entry.getValue());
            keyBuilder.setLength(0);
        }
        return newSubMap;
    }

    protected String getMappedKey(String keyOri, ParamKeyMapper mapper, StringBuilder mapperInfoSb) {
        /**
         * Transform option keys
         */
        String key = mapper.getMappedKey(keyOri);
        if (!key.equals(keyOri)) {
            mapperInfoSb.append("\n\t")
                    .append(keyOri)
                    .append(" ==> ")
                    .append(key);
        }
        return key;
    }


    @Override
    public abstract ParseResult parse(String[] input);


}
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

package com.webank.wedatasphere.linkis.cli.core.interactor.command.fitter;

import com.webank.wedatasphere.linkis.cli.common.constants.CommonConstants;
import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdOption;
import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdTemplate;
import com.webank.wedatasphere.linkis.cli.common.exception.LinkisClientRuntimeException;
import com.webank.wedatasphere.linkis.cli.common.exception.error.ErrorLevel;
import com.webank.wedatasphere.linkis.cli.core.exception.CommandException;
import com.webank.wedatasphere.linkis.cli.core.exception.error.CommonErrMsg;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.template.option.Flag;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.template.option.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * @description: fit command arguments and fill them into {@link com.webank.wedatasphere.linkis.cli.common.entity.command.CmdTemplate}.
 * Stores all that cannot be parsed.
 */
public abstract class AbstractFitter implements Fitter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractFitter.class);

    /**
     * Parse arguments based on template.
     * Any redundant argument will be stored for further parsing.
     *
     * @throws LinkisClientRuntimeException
     */
    @Override
    public abstract FitterResult fit(String[] inputs, CmdTemplate templateCopy) throws LinkisClientRuntimeException;

    protected CmdTemplate doFit(String[] args, CmdTemplate templateCopy, List<String> remains) throws LinkisClientRuntimeException {
        String msg = "Parsing command: \"{0}\" into template: \"{1}\"";
        logger.info(MessageFormat.format(msg, StringUtils.join(args, " "), templateCopy.getCmdType()));

        doFit(args, 0, templateCopy, remains);

        return templateCopy;
    }


    /**
     * Parse arguments one by one.
     * If an Option/Flag is not defined in 'commandTemplate', then record it in 'remains'
     * If all parameters defined in 'commandTemplate' are already set, then any other Parameter is recorded in 'remains'
     *
     * @throws LinkisClientRuntimeException when some argument is not configured
     */
    private final void doFit(final String[] args, int start, CmdTemplate templateCopy, List<String> remains) throws LinkisClientRuntimeException {
        doFit(args, start, 0, templateCopy, remains);
    }

    private final void doFit(final String[] args, final int argIdx, final int paraIdx, CmdTemplate templateCopy, List<String> remains)
            throws LinkisClientRuntimeException {
        if (args.length <= argIdx || args.length <= paraIdx) {
            return;
        }

        if (FitterUtils.isOption(args[argIdx])) {

            int index = setOptionValue(args, argIdx, templateCopy, remains);
            // fit from the new index
            doFit(args, index, paraIdx, templateCopy, remains);
        } else {
            int index = setParameterValue(args, argIdx, paraIdx, templateCopy, remains);
            // fit from argIdx + 1
            doFit(args, index, paraIdx + 1, templateCopy, remains);
        }
    }


    /**
     * If an input option is not defined by template. Then its name and value(if exists) is recorded in 'remains'
     *
     * @param args            java options
     * @param index           argument index
     * @param commandTemplate
     * @return next argument index
     * @throws LinkisClientRuntimeException when some argument is not configured
     */
    private final int setOptionValue(final String[] args, final int index, CmdTemplate commandTemplate, List<String> remains) throws LinkisClientRuntimeException {
        int next = index + 1;
        String arg = args[index];
        Map<String, CmdOption<?>> optionsMap = commandTemplate.getOptionsMap();
        if (optionsMap.containsKey(args[index])) {
            CmdOption<?> cmdOption = optionsMap.get(arg);
            if (cmdOption instanceof Flag) {
                try {
                    cmdOption.setValueWithStr("true");
                } catch (IllegalArgumentException ie) {
                    String msg = MessageFormat.format("Illegal Arguement \"{0}\" for option \"{1}\"", args[next], cmdOption.getParamName());
                    throw new CommandException("CMD0010", ErrorLevel.ERROR, CommonErrMsg.TemplateFitErr, commandTemplate.getCmdType(), msg);
                }
                return next;
            } else if (cmdOption instanceof CmdOption<?>) {
                if (next >= args.length || FitterUtils.isOption(args[next])) {
                    String msg = MessageFormat.format("Cannot parse command: option \"{0}\" is specified without value.", arg);
                    throw new CommandException("CMD0011", ErrorLevel.ERROR, CommonErrMsg.TemplateFitErr, commandTemplate.getCmdType(), msg);
                }
                try {
                    cmdOption.setValueWithStr(args[next]);
                } catch (IllegalArgumentException ie) {
                    String msg = MessageFormat.format("Illegal Arguement \"{0}\" for option \"{1}\". Msg: {2}", args[next], cmdOption.getParamName(), ie.getMessage());
                    throw new CommandException("CMD0010", ErrorLevel.ERROR, CommonErrMsg.TemplateFitErr, commandTemplate.getCmdType(), msg);
                }

                return next + 1;
            } else {
                throw new CommandException("CMD0010", ErrorLevel.ERROR, CommonErrMsg.TemplateFitErr, "Failed to set option value: optionMap contains objects that is not Option!");
            }
        } else {
            remains.add(arg);
            if (next < args.length && !FitterUtils.isOption(args[next])) {
                remains.add(args[next]);
                return next + 1;
            } else {
                return next;
            }
        }
    }

    /**
     * If number of user input parameter is larger than what's defined in template,
     * then set parameter value based on input order and record the rests in 'remains'.
     *
     * @param args    java options
     * @param argIdx  argument index
     * @param paraIdx index of Parameter
     * @return next argument index
     * @throws LinkisClientRuntimeException
     */
    private final int setParameterValue(final String[] args, final int argIdx, final int paraIdx, CmdTemplate templateCopy, List<String> remains)
            throws LinkisClientRuntimeException {
        List<CmdOption<?>> options = templateCopy.getOptions();
        List<CmdOption<?>> parameters = new ArrayList<>();
        for (CmdOption<?> option : options) {
            if (option instanceof Parameter<?>) {
                parameters.add(option);
            }
        }
        if (parameters.size() <= paraIdx) {
            remains.add(args[argIdx]);
            return argIdx + 1;
        }
        CmdOption<?> cmdOption = parameters.get(paraIdx);
        if (!(cmdOption instanceof Parameter<?>)) {
            throw new CommandException("CMD001", ErrorLevel.ERROR, CommonErrMsg.TemplateFitErr, "Failed to set param value: parameters contains objects that is not Parameter!");

        }
        Parameter<?> param = (Parameter<?>) cmdOption;
        if (param.accepctArrayValue()) {
            String[] args2 = Arrays.copyOfRange(args, argIdx, args.length);
            param.setValueWithStr(StringUtils.join(args2, CommonConstants.ARRAY_SEQ));
            return args.length;
        } else {
            parameters.get(paraIdx).setValueWithStr(args[argIdx]);
            return argIdx + 1;
        }
    }

}

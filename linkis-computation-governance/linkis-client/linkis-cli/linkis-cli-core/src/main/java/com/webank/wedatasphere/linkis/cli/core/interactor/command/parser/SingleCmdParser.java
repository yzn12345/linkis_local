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


import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdTemplate;
import com.webank.wedatasphere.linkis.cli.common.entity.command.Params;
import com.webank.wedatasphere.linkis.cli.common.exception.error.ErrorLevel;
import com.webank.wedatasphere.linkis.cli.core.exception.CommandException;
import com.webank.wedatasphere.linkis.cli.core.exception.error.CommonErrMsg;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.fitter.FitterResult;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.parser.result.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


public class SingleCmdParser extends AbstarctParser {
    private static final Logger logger = LoggerFactory.getLogger(SingleCmdParser.class);

    @Override
    public ParseResult parse(String[] input) {
        checkInit();

        if (input == null || input.length == 0) {
            throw new CommandException("CMD0015", ErrorLevel.ERROR, CommonErrMsg.ParserParseErr, template.getCmdType(), "nothing to parse");
        }

        FitterResult result = fitter.fit(input, template);

        String[] remains = result.getRemains();

        if (remains != null && remains.length != 0) {
            throw new CommandException("CMD0022", ErrorLevel.ERROR, CommonErrMsg.ParserParseErr, template.getCmdType(), "Cannot parse argument(s): " + Arrays.toString(remains) + ". Please check help message");
        }

        CmdTemplate parsedCopyOfTemplate = result.getParsedTemplateCopy();
        Params param = templateToParams(parsedCopyOfTemplate, mapper);

        return new ParseResult(parsedCopyOfTemplate, param, remains);
    }
}
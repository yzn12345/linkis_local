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

package com.webank.wedatasphere.linkis.cli.core.interactor.command.parser.result;

import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdTemplate;
import com.webank.wedatasphere.linkis.cli.common.entity.command.Params;


public class ParseResult {
    CmdTemplate parsedTemplateCopy;
    Params params;
    String[] remains;

    public ParseResult() {
    }

    public ParseResult(CmdTemplate parsedTemplateCopy, Params params, String[] remains) {
        this.parsedTemplateCopy = parsedTemplateCopy;
        this.params = params;
        this.remains = remains;
    }

    public CmdTemplate getParsedTemplateCopy() {
        return parsedTemplateCopy;
    }

    public void setParsedTemplateCopy(CmdTemplate parsedTemplateCopy) {
        this.parsedTemplateCopy = parsedTemplateCopy;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public String[] getRemains() {
        return remains;
    }

    public void setRemains(String[] remains) {
        this.remains = remains;
    }
}
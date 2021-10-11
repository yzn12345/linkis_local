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

package com.webank.wedatasphere.linkis.cli.application.interactor.command;

import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdType;
import com.webank.wedatasphere.linkis.cli.core.constants.Constants;

/**
 * @description: Implements {@link CmdType}.
 */
public enum LinkisCmdType implements CmdType {

    UNIVERSAL(Constants.UNIVERSAL_SUBCMD, 1, Constants.UNIVERSAL_SUBCMD_DESC);

    private int id;
    private String name;
    private String desc;

    LinkisCmdType(String name, int id) {
        this.id = id;
        this.name = name;
        this.desc = null;
    }

    LinkisCmdType(String name, int id, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }


}
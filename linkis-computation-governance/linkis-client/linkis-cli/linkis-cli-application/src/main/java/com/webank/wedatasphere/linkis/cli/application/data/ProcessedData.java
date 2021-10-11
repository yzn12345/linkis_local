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

package com.webank.wedatasphere.linkis.cli.application.data;

import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdType;
import com.webank.wedatasphere.linkis.cli.core.interactor.var.VarAccess;


public class ProcessedData {
    String cid;
    CmdType cmdType;
    VarAccess stdVarAccess;
    VarAccess sysVarAccess;

    public ProcessedData(String cid, CmdType cmdType, VarAccess stdVarAccess, VarAccess sysVarAccess) {
        this.cid = cid;
        this.cmdType = cmdType;
        this.stdVarAccess = stdVarAccess;
        this.sysVarAccess = sysVarAccess;
    }

    public String getCid() {
        return cid;
    }

    public CmdType getCmdType() {
        return cmdType;
    }

    public VarAccess getStdVarAccess() {
        return stdVarAccess;
    }

    public VarAccess getSysVarAccess() {
        return sysVarAccess;
    }

}
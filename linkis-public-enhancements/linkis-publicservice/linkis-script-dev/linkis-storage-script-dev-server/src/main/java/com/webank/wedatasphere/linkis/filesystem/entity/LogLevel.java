/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.filesystem.entity;

import com.webank.wedatasphere.linkis.filesystem.util.WorkspaceUtil;

public class LogLevel {

    private Type type;

    public LogLevel(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        ERROR(WorkspaceUtil.errorReg), WARN(WorkspaceUtil.warnReg), INFO(WorkspaceUtil.infoReg), ALL(WorkspaceUtil.allReg);
        private String reg;

        Type(String reg) {
            this.reg = reg;
        }

        public String getReg() {
            return this.reg;
        }
    }
}

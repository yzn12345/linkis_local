/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.manager.common.protocol.engine;



public class EngineStopResponse {

    public EngineStopResponse() {}

    public EngineStopResponse(boolean stopStatus, String msg) {
        this.stopStatus = stopStatus;
        this.msg = msg;
    }

    private boolean stopStatus;
    private String msg;

    public boolean getStopStatus() {
        return stopStatus;
    }

    public void setStopStatus(boolean status) {
        this.stopStatus = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

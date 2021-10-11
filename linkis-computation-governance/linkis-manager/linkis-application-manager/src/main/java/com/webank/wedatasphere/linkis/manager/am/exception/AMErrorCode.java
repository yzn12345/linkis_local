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

package com.webank.wedatasphere.linkis.manager.am.exception;



public enum AMErrorCode {

    QUERY_PARAM_NULL(21001,"query param cannot be null(请求参数不能为空)"),

    UNSUPPORT_VALUE(21002,"unsupport value(不支持的值类型)"),

    PARAM_ERROR(210003,"param error(参数错误)"),

    NOT_EXISTS_ENGINE_CONN(210003,"Not exists EngineConn(不存在的引擎)"),

    AM_CONF_ERROR(210004,"AM configuration error(AM配置错误)")
    ;


    AMErrorCode(int errorCode, String message) {
        this.code = errorCode;
        this.message = message;
    }

    private int code;

    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

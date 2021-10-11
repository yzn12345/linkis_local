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

package com.webank.wedatasphere.linkis.cli.common.exception;

import com.webank.wedatasphere.linkis.cli.common.exception.error.ErrorLevel;
import com.webank.wedatasphere.linkis.cli.common.exception.error.ErrorMsg;

import java.text.MessageFormat;

/**
 * @description: RuntimeException for BdpLinkisClient
 */
public class LinkisClientRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 342134234324357L;

    /**
     * ErrorCode
     **/
    private String code;

    /**
     * Description
     **/
    private String msg;
    /**
     * extra message
     **/
    private String extMsg;

    private ErrorLevel level;

    private ErrorMsg errMsg;

    public LinkisClientRuntimeException(String code, ErrorLevel level, ErrorMsg errMsg, String param[], String... extMsg) {
        super(null == code ? "" : code);
        init(code, level, errMsg, param, extMsg);
    }

    public LinkisClientRuntimeException(String code, ErrorLevel level, ErrorMsg errMsg, Object... paramsList) {
        super(null == code ? "" : code);
        this.code = code;
        Object[] params;
        if ((paramsList != null) && (paramsList.length > 0)
                && ((paramsList[(paramsList.length - 1)] instanceof Throwable))) {
            Object[] newParam = new Object[paramsList.length - 1];
            System.arraycopy(paramsList, 0, newParam, 0, newParam.length);
            params = newParam;
            super.initCause((Throwable) paramsList[(paramsList.length - 1)]);
        } else {
            params = paramsList;
            super.initCause(null);
        }
        this.code = null == code ? null : code;
        this.level = null == level ? ErrorLevel.ERROR : level;
        this.msg = null == errMsg ? "" : MessageFormat.format(errMsg.getMsgTemplate(), params);
        this.extMsg = this.msg;
    }

    public LinkisClientRuntimeException(String code, ErrorLevel level, ErrorMsg errMsg, Throwable e, String param[], String... extMsg) {
        super(null == code ? "" : code, e);
        init(code, level, errMsg, param, extMsg);
    }

    private void init(String code, ErrorLevel level, ErrorMsg errMsg, Object param[], String... extMsg) {
        this.errMsg = errMsg;
        this.code = null == code ? null : code;
        this.level = null == level ? ErrorLevel.ERROR : level;
        this.msg = null == errMsg ? "" : MessageFormat.format(errMsg.getMsgTemplate(), param);
        StringBuilder builder = new StringBuilder(100);
        builder.append(this.msg);
        if (null != extMsg) {
            for (String ext : extMsg) {
                builder.append("[").append(ext).append("]");
            }
        }
        this.extMsg = builder.toString();
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getExtMsg() {
        return extMsg;
    }

    public ErrorLevel getLevel() {
        return level;
    }

    public ErrorMsg getErrMsg() {
        return errMsg;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "," + this.extMsg;
    }

}
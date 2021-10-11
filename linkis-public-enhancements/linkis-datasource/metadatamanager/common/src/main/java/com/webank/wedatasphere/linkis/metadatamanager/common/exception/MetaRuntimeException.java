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

package com.webank.wedatasphere.linkis.metadatamanager.common.exception;

import com.webank.wedatasphere.linkis.common.exception.WarnException;

public class MetaRuntimeException extends WarnException {
    private static final int ERROR_CODE = 99900;
    public MetaRuntimeException(String desc) {
        super(ERROR_CODE, desc);
    }

    public MetaRuntimeException(String desc, String ip, int port, String serviceKind) {
        super(ERROR_CODE, desc, ip, port, serviceKind);
    }

}

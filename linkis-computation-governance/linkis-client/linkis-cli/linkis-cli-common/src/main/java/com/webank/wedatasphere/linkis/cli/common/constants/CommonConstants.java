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

package com.webank.wedatasphere.linkis.cli.common.constants;


public class CommonConstants {
    public static final String DUMMY_IDENTIFIER = "dummy";
    public static final String ARRAY_SEQ = "@#@";
    public static final String ARRAY_SEQ_REGEX = "(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
    public static final int MAX_NUM_OF_COMMAND_ARGUEMENTS = 10;

    public static final String CONFIG_DIR = "config.path";
    public static final String[] CONFIG_EXTENSION = {"properties"};

    public static final String SYSTEM_PROPERTIES_IDENTIFIER = "SYS_PROP";
    public static final String SYSTEM_ENV_IDENTIFIER = "SYS_ENV";

}

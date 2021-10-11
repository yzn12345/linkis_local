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

package com.webank.wedatasphere.linkis.cli.core.constants;


public class Constants {


    public static final Long JOB_QUERY_SLEEP_MILLS = 2000l;
    public static final Integer REQUEST_MAX_RETRY_TIME = 3;

    public static final String UNIVERSAL_SUBCMD = "linkis-cli";
    public static final String UNIVERSAL_SUBCMD_DESC = "command for all types of jobs supported by Linkis";

    public static final String SUCCESS_INDICATOR = "############Execute Success!!!########";
    public static final String FAILURE_INDICATOR = "############Execute Error!!!########";
}
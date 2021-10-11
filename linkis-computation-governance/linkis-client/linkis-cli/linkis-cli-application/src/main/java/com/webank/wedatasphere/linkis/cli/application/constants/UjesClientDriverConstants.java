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

package com.webank.wedatasphere.linkis.cli.application.constants;


public class UjesClientDriverConstants {

    public static final int EXCEPTION_CHECK_AFTER_PAGE = 10000;

    public static final String QUERY_PAGE_SIZE_NAME = "pageSize";
    public static final int QUERY_PAGE_SIZE_DEFAULT_VALUE = 100;

    public static final Long DRIVER_QUERY_SLEEP_MILLS = 500l;
    public static final Integer DRIVER_REQUEST_MAX_RETRY_TIME = 3;

    public static final String QUERY_STATUS_NAME = "status";
    public static final String QUERY_START_DATE_MILLS_NAME = "startDateMills";
    public static final String QUERY_END_DATE_MILLS_NAME = "endDateMills";
    public static final String QUERY_PAGE_NOW_NAME = "pageNow";


    public static final String LINKIS_JOB_LOG_FINISH_INDICATOR = "Your job completed with";

    public static final Integer LINKIS_JOB_EXEC_RESULT_EXCEPTION_CODE = 10905;

    public static final Integer MAX_LOG_SIZE = -1;
    public static final Integer IDX_FOR_LOG_TYPE_ALL = 3;// 0: Error 1: WARN 2:INFO 3: ALL

    public static final int DEFAULT_PAGE_SIZE = 500;

    public static final String DEFAULT_SPARK_ENGINE = "spark-2.4.3";
    public static final String DEFAULT_HIVE_ENGINE = "hive-1.2.1";


}

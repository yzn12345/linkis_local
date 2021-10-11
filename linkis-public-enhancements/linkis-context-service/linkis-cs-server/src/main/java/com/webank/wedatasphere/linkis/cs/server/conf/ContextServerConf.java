/*
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
 */

package com.webank.wedatasphere.linkis.cs.server.conf;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import com.webank.wedatasphere.linkis.common.conf.TimeType;

public class ContextServerConf {

    public final static String KEYWORD_SCAN_PACKAGE = CommonVars.apply("wds.linkis.cs.keyword.scan.package","com.webank.wedatasphere.linkis.cs").getValue();

    public final static int CS_SCHEDULER_MAX_RUNNING_JOBS = CommonVars.apply("wds.linkis.cs.running.jobs.max",100).getValue();
    public final static long CS_SCHEDULER_MAX_ASK_EXECUTOR_TIMES = CommonVars.apply("wds.linkis.cs.ask.executor.times.max",new TimeType("1s")).getValue().toLong();
}

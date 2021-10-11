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

package com.webank.wedatasphere.linkis.jobhistory.cache.impl;

import com.webank.wedatasphere.linkis.jobhistory.cache.QueryCacheManager;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class ScheduledCleanJob extends QuartzJobBean {

    private static Logger logger = LoggerFactory.getLogger(ScheduledCleanJob.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("Started cache cleaning job.");
        QueryCacheManager queryCacheManager = (QueryCacheManager) jobExecutionContext.getJobDetail().getJobDataMap().get(QueryCacheManager.class.getName());
        queryCacheManager.cleanAll();
        logger.info("Finished cache cleaning job.");
    }
}

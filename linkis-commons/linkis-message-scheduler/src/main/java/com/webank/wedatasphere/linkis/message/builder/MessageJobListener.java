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

package com.webank.wedatasphere.linkis.message.builder;

import com.webank.wedatasphere.linkis.common.utils.JavaLog;
import com.webank.wedatasphere.linkis.scheduler.listener.JobListener;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;

import java.util.concurrent.locks.LockSupport;


public class MessageJobListener extends JavaLog implements JobListener {

    @Override
    public void onJobScheduled(Job job) {

    }

    @Override
    public void onJobInited(Job job) {

    }

    @Override
    public void onJobWaitForRetry(Job job) {

    }

    @Override
    public void onJobRunning(Job job) {

    }

    @Override
    public void onJobCompleted(Job job) {
        if (job instanceof DefaultMessageJob) {
            LockSupport.unpark(((DefaultMessageJob) job).getBlockThread());
        }
    }

}

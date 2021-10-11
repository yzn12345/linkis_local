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

package com.webank.wedatasphere.linkis.jobhistory.transitional;


public enum TaskStatus {
    /*
    This should be up-to-date with status in entrance job
     */
    Inited, WaitForRetry, Scheduled, Running, Succeed, Failed, Cancelled, Timeout;

    public static boolean isComplete(TaskStatus taskStatus){
        if(taskStatus == Succeed || taskStatus == Failed || taskStatus == Cancelled || taskStatus == Timeout){
            return true;
        }else{
            return false;
        }
    }
}

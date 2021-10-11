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

package com.webank.wedatasphere.linkis.errorcode.client.manager;

import com.webank.wedatasphere.linkis.errorcode.client.synchronizer.LinkisErrorCodeSynchronizer;
import com.webank.wedatasphere.linkis.errorcode.common.LinkisErrorCode;

import java.util.List;


public class LinkisErrorCodeManager {

    private static LinkisErrorCodeManager linkisErrorCodeManager;



    private final LinkisErrorCodeSynchronizer linkisErrorCodeSynchronizer = LinkisErrorCodeSynchronizer.getInstance();

    private LinkisErrorCodeManager(){

    }

    public static LinkisErrorCodeManager getInstance(){
        if (linkisErrorCodeManager == null){
            synchronized (LinkisErrorCodeManager.class){
                if (linkisErrorCodeManager == null){
                    linkisErrorCodeManager = new LinkisErrorCodeManager();
                }
            }
        }
        return linkisErrorCodeManager;
    }



    public List<LinkisErrorCode> getLinkisErrorCodes(){
        return linkisErrorCodeSynchronizer.synchronizeErrorCodes();
    }

}

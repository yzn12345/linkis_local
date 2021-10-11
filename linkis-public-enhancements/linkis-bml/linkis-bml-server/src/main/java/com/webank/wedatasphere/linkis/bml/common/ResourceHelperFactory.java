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
package com.webank.wedatasphere.linkis.bml.common;


import com.webank.wedatasphere.linkis.bml.conf.BmlServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceHelperFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceHelperFactory.class);

    private static final boolean IS_HDFS = (Boolean) BmlServerConfiguration.BML_IS_HDFS().getValue();

    private static final ResourceHelper HDFS_RESOURCE_HELPER = new HdfsResourceHelper();

    private static final ResourceHelper LOCAL_RESOURCE_HELPER = new LocalResourceHelper();


    public static ResourceHelper getResourceHelper(){
        if (IS_HDFS){
            LOGGER.info("will store resource in hdfs");
            return HDFS_RESOURCE_HELPER;
        }else{
            LOGGER.info("will store resource in local");
            return LOCAL_RESOURCE_HELPER;
        }
    }
}

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

package com.webank.wedatasphere.linkis.metadata.util;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import sun.misc.BASE64Decoder;

import java.io.File;

public class HiveUtils {

    static Logger logger = Logger.getLogger(HiveUtils.class);

    public static Configuration getDefaultConf(String userName) {
        Configuration conf = new Configuration();
        String hiveConfPath = DWSConfig.HIVE_CONF_DIR.getValue();
        if (StringUtils.isNotEmpty(hiveConfPath)) {
            logger.info("Load hive configuration from " + hiveConfPath);
            conf.addResource(new Path(hiveConfPath + File.separator + "hive-site.xml"));
        } else {
            conf.addResource("hive-site.xml");
        }
        return conf;
    }

    public static String decode(String str) {
        BASE64Decoder decoder = new BASE64Decoder();
        String res = str;
        try {
            res = new String(decoder.decodeBuffer(str));
        } catch (Throwable e) {
            logger.error(str + " decode failed", e);
        }
        return res;
    }


}

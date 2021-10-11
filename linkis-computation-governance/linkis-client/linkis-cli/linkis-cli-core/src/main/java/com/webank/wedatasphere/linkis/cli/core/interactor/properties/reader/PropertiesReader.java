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

package com.webank.wedatasphere.linkis.cli.core.interactor.properties.reader;

import java.util.Properties;

/**
 * ent
 *
 * @description: read properties. one instance of PropertiesReader corresponds to one source (file/sys_prop/sys_env etc)
 */
public interface PropertiesReader {

    String getPropsId();

    PropertiesReader setPropsId(String identifier);

    String getPropsPath();

    PropertiesReader setPropsPath(String propertiesPath);

    Properties getProperties();

    void checkInit();
}

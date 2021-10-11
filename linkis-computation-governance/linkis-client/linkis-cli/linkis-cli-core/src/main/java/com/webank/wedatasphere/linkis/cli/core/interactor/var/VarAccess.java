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

package com.webank.wedatasphere.linkis.cli.core.interactor.var;

/**
 * @description: Retrieve value from input command/config/sys_prop/sys_env etc.
 * order should be: command > user config > default config > default
 */
public interface VarAccess {

    void checkInit();

    <T> T getVar(Class<T> clazz, String key);

    <T> T getVarOrDefault(Class<T> clazz, String key, T defaultValue);

    boolean hasVar(String key);

    String[] getAllVarKeys();

}
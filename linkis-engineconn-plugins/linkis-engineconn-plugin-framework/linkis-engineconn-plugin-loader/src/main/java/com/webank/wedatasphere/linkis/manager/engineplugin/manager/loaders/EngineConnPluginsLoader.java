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

package com.webank.wedatasphere.linkis.manager.engineplugin.manager.loaders;

import com.webank.wedatasphere.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInstance;
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineTypeLabel;


public interface EngineConnPluginsLoader {


    /**
     * Get plugin( will get from the cache first )
     * @param engineTypeLabel engine type label
     * @return enginePlugin and classloader (you must not to hold the instance, avoid OOM)
     */
    EngineConnPluginInstance getEngineConnPlugin(EngineTypeLabel engineTypeLabel) throws Exception;
    /**
     * Load plugin without caching ( will force to update the cache )
     * @param engineTypeLabel engine type label
     * @return enginePlugin and classloader (you must not to hold the instance, avoid OOM)
     */
    EngineConnPluginInstance loadEngineConnPlugin(EngineTypeLabel engineTypeLabel) throws Exception;

}

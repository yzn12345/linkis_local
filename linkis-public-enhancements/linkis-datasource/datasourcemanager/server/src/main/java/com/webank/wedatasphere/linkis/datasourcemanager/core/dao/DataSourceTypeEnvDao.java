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

package com.webank.wedatasphere.linkis.datasourcemanager.core.dao;

import org.apache.ibatis.annotations.Param;

public interface DataSourceTypeEnvDao {

    /**
     * Insert relation between type and environment
     * @param dataSourceTypeId data source type
     * @param dataSourceEnvId data source env
     */
    void insertRelation(@Param("dataSourceTypeId") Long dataSourceTypeId,
                        @Param("dataSourceEnvId") Long dataSourceEnvId);

    /**
     * Remove relations by environment id
     * @param envId
     * @return
     */
    int removeRelationsByEnvId(Long envId);
}

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

package com.webank.wedatasphere.linkis.datasourcemanager.core.service.impl;

import com.webank.wedatasphere.linkis.datasourcemanager.core.dao.DataSourceTypeDao;
import com.webank.wedatasphere.linkis.datasourcemanager.core.dao.DataSourceParamKeyDao;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceType;
import com.webank.wedatasphere.linkis.datasourcemanager.core.service.DataSourceRelateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataSourceRelateServiceImpl implements DataSourceRelateService {

    @Autowired
    private DataSourceParamKeyDao paramKeyDao;

    @Autowired
    private DataSourceTypeDao dataSourceTypeDao;

    @Override
    public List<DataSourceParamKeyDefinition> getKeyDefinitionsByType(Long dataSourceTypeId, DataSourceParamKeyDefinition.Scope scope) {
        return paramKeyDao.listByDataSourceTypeAndScope(dataSourceTypeId, scope);
    }

    @Override
    public List<DataSourceParamKeyDefinition> getKeyDefinitionsByType(Long dataSourceTypeId) {
        return paramKeyDao.listByDataSourceType(dataSourceTypeId);
    }

    @Override
    public List<DataSourceType> getAllDataSourceTypes() {
        return dataSourceTypeDao.getAllTypes();
    }

    @Override
    public DataSourceType getDataSourceType(Long typeId) {
        return dataSourceTypeDao.selectOne(typeId);
    }
}

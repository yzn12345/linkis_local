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

package com.webank.wedatasphere.linkis.metadata.dao;


import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqField;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqImport;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqLineage;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface MdqDao {

    void activateTable(Long tableId);
    
    MdqTable selectTableByName(@Param("database") String database, @Param("tableName") String tableName, @Param("user") String user);

    List<MdqField> listMdqFieldByTableId(Long tableId);

    void insertTable(MdqTable table);

    void insertFields(@Param("mdqFieldList") List<MdqField> mdqFieldList);

    void insertImport(MdqImport mdqImport);

    void insertLineage(MdqLineage mdqLineage);

    MdqTable selectTableForUpdate(@Param("database") String database, @Param("tableName") String tableName);

    void deleteTableBaseInfo(Long id);
}

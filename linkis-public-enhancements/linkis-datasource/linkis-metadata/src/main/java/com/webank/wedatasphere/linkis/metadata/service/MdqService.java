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

package com.webank.wedatasphere.linkis.metadata.service;



import com.webank.wedatasphere.linkis.metadata.domain.mdq.bo.MdqTableBO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.vo.MdqTableBaseInfoVO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.vo.MdqTableFieldsInfoVO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.vo.MdqTablePartitionStatisticInfoVO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.vo.MdqTableStatisticInfoVO;
import com.webank.wedatasphere.linkis.metadata.exception.MdqIllegalParamException;

import java.io.IOException;
import java.util.List;


public interface MdqService {

    /**
     * 激活表操作，sparkEngine执行成功后使用rpc请求调用，参数是数据库主键id
     * @param tableId
     */
    @Deprecated
    void activateTable(Long tableId);

    /**
     * 传入MdqTableBO  由sparkEnginerpc请求jsonStrig序列化得到，执行插入数据库的记录，返回数据库主键id，用来做激活的标识
     * @param mdqTableBO
     * @return
     */
    Long persistTable(MdqTableBO mdqTableBO, String userName);

    MdqTableStatisticInfoVO getTableStatisticInfo(String database, String tableName, String user) throws IOException;

    /**
     * 产生sql给前台，和sparkEngine
     * @param mdqTableBO
     * @return
     */
    String displaysql(MdqTableBO mdqTableBO);

    boolean isExistInMdq(String database, String tableName, String user);

    MdqTableBaseInfoVO getTableBaseInfoFromMdq(String database, String tableName, String user);

    MdqTableBaseInfoVO getTableBaseInfoFromHive(String database, String tableName, String user);

    List<MdqTableFieldsInfoVO> getTableFieldsInfoFromMdq(String database, String tableName, String user);

    List<MdqTableFieldsInfoVO> getTableFieldsInfoFromHive(String database, String tableName, String user);

    MdqTableStatisticInfoVO getTableStatisticInfoFromHive(String database, String tableName, String user) throws IOException;

    MdqTablePartitionStatisticInfoVO getPartitionStatisticInfo(String database, String tableName, String userName, String partitionName) throws IOException, MdqIllegalParamException;
}

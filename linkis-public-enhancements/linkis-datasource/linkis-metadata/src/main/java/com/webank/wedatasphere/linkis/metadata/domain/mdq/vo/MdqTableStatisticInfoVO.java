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

package com.webank.wedatasphere.linkis.metadata.domain.mdq.vo;

import java.util.Date;
import java.util.List;


public class MdqTableStatisticInfoVO {
    private Integer rowNum;
    private Integer fileNum;
    private String tableSize;
    private Integer partitionsNum;
    private Date tableLastUpdateTime;
    private Integer fieldsNum;

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public Integer getFileNum() {
        return fileNum;
    }

    public void setFileNum(Integer fileNum) {
        this.fileNum = fileNum;
    }

    public String getTableSize() {
        return tableSize;
    }

    public void setTableSize(String tableSize) {
        this.tableSize = tableSize;
    }

    public Integer getPartitionsNum() {
        return partitionsNum;
    }

    public void setPartitionsNum(Integer partitionsNum) {
        this.partitionsNum = partitionsNum;
    }

    public Date getTableLastUpdateTime() {
        return tableLastUpdateTime;
    }

    public void setTableLastUpdateTime(Date tableLastUpdateTime) {
        this.tableLastUpdateTime = tableLastUpdateTime;
    }

    public Integer getFieldsNum() {
        return fieldsNum;
    }

    public void setFieldsNum(Integer fieldsNum) {
        this.fieldsNum = fieldsNum;
    }

    public List<MdqTablePartitionStatisticInfoVO> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<MdqTablePartitionStatisticInfoVO> partitions) {
        this.partitions = partitions;
    }

    private List<MdqTablePartitionStatisticInfoVO> partitions;


}

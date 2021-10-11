/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation;

import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.deployment.YarnSessionClusterDescriptorAdapter;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.JobExecutionException;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.SqlExecutionException;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.listener.FlinkListenerGroup;
import java.util.Optional;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;


public interface JobOperation extends Operation, FlinkListenerGroup {

	JobID getJobId();

	Optional<ResultSet> getJobResult() throws SqlExecutionException;

	JobStatus getJobStatus() throws JobExecutionException;

	void cancelJob() throws JobExecutionException;

	void setClusterDescriptorAdapter(YarnSessionClusterDescriptorAdapter clusterDescriptor);

}

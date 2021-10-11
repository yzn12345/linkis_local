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
package com.webank.wedatasphere.linkis.engineconnplugin.flink.client.deployment;

import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.context.ExecutionContext;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.JobExecutionException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.hadoop.yarn.api.records.ApplicationId;

public class YarnApplicationClusterDescriptorAdapter extends ClusterDescriptorAdapter  {

    YarnApplicationClusterDescriptorAdapter(ExecutionContext executionContext) {
        super(executionContext);
    }

    public void deployCluster(String[] programArguments, String applicationClassName) throws JobExecutionException {
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(programArguments,applicationClassName);
        ClusterSpecification clusterSpecification  = this.executionContext.getClusterClientFactory().getClusterSpecification(this.executionContext.getFlinkConfig());
        YarnClusterDescriptor clusterDescriptor =  this.executionContext.createClusterDescriptor();
        try {
            ClusterClientProvider<ApplicationId> clusterClientProvider = clusterDescriptor.deployApplicationCluster(
                    clusterSpecification,
                    applicationConfiguration);
            clusterClient = clusterClientProvider.getClusterClient();
            super.clusterID  = clusterClient.getClusterId();
            super.webInterfaceUrl = clusterClient.getWebInterfaceURL();
        } catch (Exception e) {
            throw new JobExecutionException(e.getMessage());
        }

    }

    public boolean initJobId() throws Exception {
        clusterClient.listJobs().thenAccept(list -> list.forEach(jobStatusMessage -> {
            if (Objects.nonNull(jobStatusMessage.getJobId())) {
                this.setJobId(jobStatusMessage.getJobId());
            }
        })).get(CLIENT_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        return null != this.getJobId();
    }


    @Override
    public boolean isGloballyTerminalState() {
        return false;
    }

}

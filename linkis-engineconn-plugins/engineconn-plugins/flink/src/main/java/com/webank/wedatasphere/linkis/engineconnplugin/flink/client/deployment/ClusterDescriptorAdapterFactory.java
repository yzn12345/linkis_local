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
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;

/**
 * Cluster Descriptor Adapter Factory(集群交互适配器工厂)
 */
public class ClusterDescriptorAdapterFactory {

	public static ClusterDescriptorAdapter create(ExecutionContext executionContext) {
		String yarnDeploymentTarget = executionContext.getFlinkConfig().get(DeploymentOptions.TARGET);
 		ClusterDescriptorAdapter clusterDescriptorAdapter = null;
		if (YarnDeploymentTarget.PER_JOB.getName().equals(yarnDeploymentTarget)) {
			clusterDescriptorAdapter = new YarnPerJobClusterDescriptorAdapter(executionContext);
		} else if (YarnDeploymentTarget.APPLICATION.getName().equals(yarnDeploymentTarget)) {
			clusterDescriptorAdapter = new YarnApplicationClusterDescriptorAdapter(executionContext);
		} else if(YarnDeploymentTarget.SESSION.getName().equals(yarnDeploymentTarget)) {
			clusterDescriptorAdapter = new YarnSessionClusterDescriptorAdapter(executionContext);
		}
		return clusterDescriptorAdapter;
	}

}

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

package com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.impl;

import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.config.Environment;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.context.ExecutionContext;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.NonJobOperation;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.OperationUtil;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.SqlExecutionException;
import org.apache.flink.table.client.config.entries.TableEntry;
import org.apache.flink.table.client.config.entries.ViewEntry;

/**
 * Operation for DROP VIEW command.
 */
public class DropViewOperation implements NonJobOperation {
	private final FlinkEngineConnContext context;
	private final String viewName;
	private final boolean ifExists;

	public DropViewOperation(FlinkEngineConnContext context, String viewName, boolean ifExists) {
		this.context = context;
		this.viewName = viewName;
		this.ifExists = ifExists;
	}

	@Override
	public ResultSet execute() throws SqlExecutionException {
		Environment env = context.getExecutionContext().getEnvironment();
		TableEntry tableEntry = env.getTables().get(viewName);
		if (!(tableEntry instanceof ViewEntry) && !ifExists) {
			throw new SqlExecutionException("'" + viewName + "' does not exist in the current session.");
		}

		// Here we rebuild the ExecutionContext because we want to ensure that all the remaining views can work fine.
		// Assume the case:
		//   view1=select 1;
		//   view2=select * from view1;
		// If we delete view1 successfully, then query view2 will throw exception because view1 does not exist. we want
		// all the remaining views are OK, so do the ExecutionContext rebuilding to avoid breaking the view dependency.
		Environment newEnv = env.clone();
		if (newEnv.getTables().remove(viewName) != null) {
			ExecutionContext oldExecutionContext = context.getExecutionContext();
			oldExecutionContext.wrapClassLoader(tableEnv -> tableEnv.dropTemporaryView(viewName));
			// Renew the ExecutionContext.
			ExecutionContext newExecutionContext = context
				.newExecutionContextBuilder(context.getEnvironmentContext().getDefaultEnv())
				.env(newEnv)
				.sessionState(context.getExecutionContext().getSessionState())
				.build();
			context.setExecutionContext(newExecutionContext);
		}

		return OperationUtil.OK;
	}
}

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

import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.context.ExecutionContext;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.NonJobOperation;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.OperationUtil;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.SqlExecutionException;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.catalog.exceptions.CatalogException;

/**
 * Operation for USE DATABASE command.
 */
public class UseDatabaseOperation implements NonJobOperation {
	private final ExecutionContext context;
	private final String databaseName;

	public UseDatabaseOperation(FlinkEngineConnContext context, String databaseName) {
		this.context = context.getExecutionContext();
		this.databaseName = databaseName;
	}

	@Override
	public ResultSet execute() throws SqlExecutionException {
		final TableEnvironment tableEnv = context.getTableEnvironment();
		try {
			context.wrapClassLoader(() -> {
				// Rely on TableEnvironment/CatalogManager to validate input
				tableEnv.useDatabase(databaseName);
				return null;
			});
		} catch (CatalogException e) {
			throw new SqlExecutionException("Failed to switch to database " + databaseName, e);
		}
		return OperationUtil.OK;
	}
}

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
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ConstantNames;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.SqlExecutionException;
import org.apache.flink.table.api.TableEnvironment;

/**
 * Operation for SHOW CURRENT CATALOG command.
 */
public class ShowCurrentCatalogOperation implements NonJobOperation {
	private final ExecutionContext context;

	public ShowCurrentCatalogOperation(FlinkEngineConnContext context) {
		this.context = context.getExecutionContext();
	}

	@Override
	public ResultSet execute() throws SqlExecutionException {
		final TableEnvironment tableEnv = context.getTableEnvironment();
		return OperationUtil.singleStringToResultSet(
			context.wrapClassLoader(tableEnv::getCurrentCatalog), ConstantNames.SHOW_CURRENT_CATALOG_RESULT);
	}
}

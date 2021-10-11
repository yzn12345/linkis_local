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
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ColumnInfo;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ConstantNames;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ResultKind;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.flink.table.client.config.entries.TableEntry;
import org.apache.flink.table.client.config.entries.ViewEntry;
import org.apache.flink.table.types.logical.VarCharType;
import org.apache.flink.types.Row;

/**
 * Operation for SHOW VIEWS command.
 */
public class ShowViewsOperation implements NonJobOperation {

	private final ExecutionContext context;

	public ShowViewsOperation(FlinkEngineConnContext context) {
		this.context = context.getExecutionContext();
	}

	@Override
	public ResultSet execute() {
		List<Row> rows = new ArrayList<>();
		int maxNameLength = 1;

		for (Map.Entry<String, TableEntry> entry : context.getEnvironment().getTables().entrySet()) {
			if (entry.getValue() instanceof ViewEntry) {
				String name = entry.getKey();
				rows.add(Row.of(name));
				maxNameLength = Math.max(maxNameLength, name.length());
			}
		}

		return ResultSet.builder()
			.resultKind(ResultKind.SUCCESS_WITH_CONTENT)
			.columns(ColumnInfo.create(ConstantNames.SHOW_VIEWS_RESULT, new VarCharType(false, maxNameLength)))
			.data(rows)
			.build();
	}
}

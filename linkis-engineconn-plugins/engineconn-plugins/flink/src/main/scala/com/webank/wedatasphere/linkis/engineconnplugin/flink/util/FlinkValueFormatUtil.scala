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
package com.webank.wedatasphere.linkis.engineconnplugin.flink.util

import java.text.NumberFormat

import com.webank.wedatasphere.linkis.common.conf.CommonVars


object FlinkValueFormatUtil {

  val FLINK_NF_FRACTION_LENGTH = CommonVars("wds.linkis.engine.flink.fraction.length", 30)

  private val nf = NumberFormat.getInstance()
  nf.setGroupingUsed(false)
  nf.setMaximumFractionDigits(FLINK_NF_FRACTION_LENGTH.getValue)

  def formatValue(value: Any): Any = value match {
    case value: String => value.replaceAll("\n|\t", " ")
    case value: Double => nf.format(value)
    case value: Any => value.toString
    case _ => null
  }

}

/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.manager.label.entity.engine

object RunType extends Enumeration {

  type RunType = Value
  val SQL = Value("sql")
  val HIVE = Value("hql")
  val SCALA = Value("scala")
  val PYTHON = Value("python")
  val JAVA = Value("java")
  val PYSPARK = Value("py")
  val R = Value("r")
  val STORAGE = Value("out")
  val SHELL = Value("shell")
  val IO_FILE = Value("io_file")
  val IO_HDFS = Value("io_hdfs")
  val PIPELINE = Value("pipeline")
  val JDBC = Value("jdbc")
  val JAR = Value("jar")
  val APPCONN = Value("appconn")

}

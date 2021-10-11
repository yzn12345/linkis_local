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

import com.webank.wedatasphere.linkis.common.utils.Logging

import java.util

object EngineType extends Enumeration with Logging {

  type EngineType = Value

  val SPARK = Value("spark")

  val HIVE  = Value("hive")

  val PYTHON = Value("python")

  val SHELL = Value("shell")

  val JDBC = Value("jdbc")

  val IO_ENGINE_FILE = Value("io_file")

  val IO_ENGINE_HDFS = Value("io_hdfs")

  val FPS = Value("fps")

  val PIPELINE = Value("pipeline")

  val ES = Value("es")

  val PRESTO = Value("presto")

  val FLINK = Value("flink")

  val APPCONN = Value("appconn")

  def mapFsTypeToEngineType(fsType: String): String = {
    fsType match {
      case "file" =>
        EngineType.IO_ENGINE_FILE.toString
      case "hdfs" =>
        EngineType.IO_ENGINE_HDFS.toString
      case _ =>
        error(s"In method mapFsTypeToEngineType(): Invalid fsType : ${fsType}, will not convert.")
        fsType
    }
  }

  /**
   * Please add it here when new engineType was added.
   */
  def mapStringToEngineType(str: String): EngineType = str match {
    case _ if null == str || "".equals(str) => null
    case _ if SPARK.toString.equalsIgnoreCase(str) => SPARK
    case _ if HIVE.toString.equalsIgnoreCase(str) => HIVE
    case _ if PYTHON.toString.equalsIgnoreCase(str) => PYTHON
    case _ if SHELL.toString.equalsIgnoreCase(str) => SHELL
    case _ if JDBC.toString.equalsIgnoreCase(str) => JDBC
    case _ if IO_ENGINE_FILE.toString.equalsIgnoreCase(str) => IO_ENGINE_FILE
    case _ if IO_ENGINE_HDFS.toString.equalsIgnoreCase(str) => IO_ENGINE_HDFS
    case _ if PIPELINE.toString.equalsIgnoreCase(str) => PIPELINE
    case _ if ES.toString.equalsIgnoreCase(str) => ES
    case _ if PRESTO.toString.equalsIgnoreCase(str) => PRESTO
    case _ if FLINK.toString.equalsIgnoreCase(str) => FLINK
    case _ if APPCONN.toString.equals(str) => APPCONN
    case _ => null

  }

  def getAllEngineTypes(): util.List[EngineType] = {
    val list = new util.ArrayList[EngineType]()
    EngineType.values.foreach(list.add)
    list
  }

}

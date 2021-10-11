/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.engineplugin.hive.common

import java.nio.file.Paths

import com.webank.wedatasphere.linkis.manager.engineplugin.common.conf.EnvConfiguration
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hive.conf.HiveConf
import org.apache.hadoop.hive.ql.Driver

object HiveUtils {

  def jarOfClass(cls: Class[_]): Option[String] = {
    val uri = cls.getResource("/" + cls.getName.replace('.', '/') + ".class")
    if (uri != null) {
      val uriStr = uri.toString
      if (uriStr.startsWith("jar:file:")) {
        Some(uriStr.substring("jar:file:".length, uriStr.indexOf("!")))
      } else {
        None
      }
    } else {
      None
    }
  }

  def getHiveConf:HiveConf = {
    val hadoopConf: Configuration = new Configuration()
    hadoopConf.addResource(new Path(Paths.get(EnvConfiguration.HADOOP_CONF_DIR.getValue, "core-site.xml").toAbsolutePath.toFile.getAbsolutePath))
    hadoopConf.addResource(new Path(Paths.get(EnvConfiguration.HADOOP_CONF_DIR.getValue, "hdfs-site.xml").toAbsolutePath.toFile.getAbsolutePath))
    hadoopConf.addResource(new Path(Paths.get(EnvConfiguration.HADOOP_CONF_DIR.getValue, "yarn-site.xml").toAbsolutePath.toFile.getAbsolutePath))
    val hiveConf = new HiveConf(hadoopConf, classOf[Driver])
    hiveConf.addResource(new Path(Paths.get(EnvConfiguration.HIVE_CONF_DIR.getValue, "hive-site.xml").toAbsolutePath.toFile.getAbsolutePath))
    hiveConf
  }


  def main(args: Array[String]): Unit = {
    jarOfClass(classOf[Driver]).foreach(println)
  }
}




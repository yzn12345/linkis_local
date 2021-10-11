/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cli.application;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LinkisClientApplicationTest {
    private static Logger logger = LoggerFactory.getLogger(LinkisClientApplicationTest.class);

    String[] cmdStr;
    String[] cmdStr2;

    @Before
    public void before() throws Exception {
        System.setProperty("conf.root", "src/test/resources/conf/");
//        System.setProperty("user.name", "notshangda");
        cmdStr2 = new String[]{
//      "--gatewayUrl", "http://127.0.0.1:8090",
//        "--authStg", "token",
//        "--authKey", "Validation-Code",
//        "--authVal", "BML-AUTH",
//                "job",
//                "kill",
//                "-j", "1121",
//                "-submitUser", "user",
//                "-proxyUser", "user",

//        "-varMap", "name=\"tables\"",
//        "-varMap", "name=\"databases\""

        };
        cmdStr = new String[]{
                "--gatewayUrl", "http://127.0.0.1:9001",
                "--authStg", "token",
                "--authKey", "Validation-Code",
                "--authVal", "BML-AUTH",
//                "--help",
//                "--kill", "8249",
//                "--status", "123"


//                "--userConf", "src/test/resources/linkis-cli.properties",

                "-creator", "LINKISCLI",
//                "-code", "show \${test};",
//                "-codePath", "src/test/resources/test",
//                "--kill", "6795",
                "-submitUser", "hadoop",
                "-proxyUser", "hadoop",
//                "-sourceMap", "scriptPath=1234",
                "-outPath", "./data/bdp-job/test/",
//                "-labelMap", "codeType=sql",
                "-confMap", "wds.linkis.yarnqueue=q02",
//                "-confMap", "wds.linkis.yarnqueue=q02",
//                "-confMap", "spark.num.executor=3",
//                "-varMap", "wds.linkis.yarnqueue=q02",
//                "-varMap", "name=\"databases\"",

/**
 * Test different task type
*/

                "-engineType", "spark-2.4.3",
                "-codeType", "sql",
                "-code", "show tables;show tables;show tables",

//
//        "-engineType", "hive-1.2.1",
//        "-codeType", "sql",
//        "-code", "show tables;show tables;show tables;show tables;show tables;show tables;",

//        "-engineType", "spark-2.4.3",
//        "-codeType", "py",
//        "-code", "print ('hello')",

//        "-engineType", "spark-2.4.3",
//        "-codeType", "scala",
//        "-codePath", "src/test/resources/testScala.scala",


/**
 * Failed
*/
//        "-engineType", "jdbc-1",
//        "-codeType", "jdbc",
//        "-code", "show tables",

//        "-engineType", "python-python2",
//        "-codeType", "python",
////        "-code", "print(\'hello\')\nprint(\'hello\')\nprint(\'hello\') ",
        };

    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: main(String[] args)
     */
    @Test
    public void testMain() throws Exception {
//TODO: Test goes here... 
    }


    /**
     * Method: prepare()
     */
    @Test
    public void testPrepare() throws Exception {
//TODO: Test goes here... 
/*
try { 
   Method method = LinkisClientApplication.getClass().getMethod("prepare"); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: processInput(String[] args, PreparedData preparedData)
     */
    @Test
    public void testProcessInput() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = LinkisClientApplication.getClass().getMethod("processInput", String[].class, PreparedData.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); c
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: exec(ProcessedData data)
     */
    @Test
    public void testExec() throws Exception {
//        LinkisClientApplication.main(cmdStr);
//    LinkisClientApplication.main(cmdStr2);
/* 
try { 
   Method method = LinkisClientApplication.getClass().getMethod("exec", ProcessedData.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

} 

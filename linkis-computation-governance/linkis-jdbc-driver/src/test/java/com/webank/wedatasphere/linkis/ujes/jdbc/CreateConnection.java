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
package com.webank.wedatasphere.linkis.ujes.jdbc;



/*
 * Notice:
 * if you want to test this module,you must rewrite default parameters and SQL we used for local test
 * */

import java.sql.DriverManager;
import java.sql.SQLException;

public class CreateConnection {

    private static UJESSQLConnection conn;

    public static UJESSQLConnection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.webank.wedatasphere.linkis.ujes.jdbc.UJESSQLDriver");
        conn = (UJESSQLConnection) DriverManager.getConnection("jdbc:linkis://hostname:port","username","password");
        return conn;
    }
}


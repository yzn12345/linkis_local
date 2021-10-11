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

package com.webank.wedatasphere.linkis.storage.excel;


import com.webank.wedatasphere.linkis.storage.utils.StorageUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class XlsUtils {
    private final static Logger LOG = LoggerFactory
            .getLogger(XlsUtils.class);

    public static List<List<String>> getBasicInfo(InputStream inputStream){
        List<List<String>> res = new ArrayList<>();
        FirstRowDeal firstRowDeal = new FirstRowDeal();
        ExcelXlsReader xlsReader = new ExcelXlsReader();
        try {
            xlsReader.init(firstRowDeal, inputStream);
            xlsReader.process();
        }catch (ExcelAnalysisException e) {

            res.add(firstRowDeal.getSheetNames());
            res.add(firstRowDeal.getRow());

            LOG.info("Finshed to get xls Info");

        } catch (Exception e){
            LOG.error("Failed to parse xls: ", e);
        } finally {
            xlsReader.close();
        }
        return  res;
    }

    public static String excelToCsv(InputStream inputStream, FileSystem fs, Boolean hasHeader, List<String> sheetNames) throws Exception{
        String hdfsPath = "/tmp/"  + StorageUtils.getJvmUser() + "/" + System.currentTimeMillis() + ".csv";
        ExcelXlsReader xlsReader = new ExcelXlsReader();
        RowToCsvDeal rowToCsvDeal = new RowToCsvDeal();
        OutputStream out = null;
        try {
            out = fs.create(new Path(hdfsPath));
            rowToCsvDeal.init(hasHeader, sheetNames, out);
            xlsReader.init(rowToCsvDeal, inputStream);
            xlsReader.process();
        } catch (IOException e) {
            LOG.error("Failed to excel to csv", e);
            throw e;
        } finally {
            if(out != null)
                out.close();
            xlsReader.close();
        }
        return hdfsPath;
    }
}

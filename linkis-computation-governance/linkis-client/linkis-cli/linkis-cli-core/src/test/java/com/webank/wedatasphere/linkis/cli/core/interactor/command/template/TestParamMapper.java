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

package com.webank.wedatasphere.linkis.cli.core.interactor.command.template;

import com.webank.wedatasphere.linkis.cli.core.constants.TestConstants;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.parser.transformer.ParamKeyMapper;


public class TestParamMapper extends ParamKeyMapper {

    @Override
    public void initMapperRules() {
        super.updateMapping(TestConstants.SPARK, "converted.cmd");
        super.updateMapping(TestConstants.PARAM_COMMON_ARGS, "converted.args");
        super.updateMapping(TestConstants.PARAM_COMMON_SPLIT, "converted.split");
//    super.updateMapping("key1", "spark.cmd"); //should throw exception
//    super.updateMapping("TestConstants.PARAM_SPARK_CMD", "spark.cmd");
//    super.updateMapping("TestConstants.PARAM_SPARK_CMD", "spark.cmd");

    }
}
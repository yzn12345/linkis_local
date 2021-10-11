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

package com.webank.wedatasphere.linkis.cli.application.interactor.command.template;

import com.webank.wedatasphere.linkis.cli.application.constants.AppConstants;
import com.webank.wedatasphere.linkis.cli.application.constants.LinkisClientKeys;
import com.webank.wedatasphere.linkis.cli.application.data.ProcessedData;
import com.webank.wedatasphere.linkis.cli.application.utils.Utils;
import com.webank.wedatasphere.linkis.cli.common.constants.CommonConstants;
import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdTemplate;
import com.webank.wedatasphere.linkis.cli.common.entity.command.Params;
import com.webank.wedatasphere.linkis.cli.common.entity.properties.ClientProperties;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.fitter.SingleTplFitter;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.parser.Parser;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.parser.SingleCmdParser;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.parser.result.ParseResult;
import com.webank.wedatasphere.linkis.cli.core.interactor.properties.PropertiesLoader;
import com.webank.wedatasphere.linkis.cli.core.interactor.properties.PropsFilesScanner;
import com.webank.wedatasphere.linkis.cli.core.interactor.properties.StdPropsLoader;
import com.webank.wedatasphere.linkis.cli.core.interactor.properties.reader.PropertiesReader;
import com.webank.wedatasphere.linkis.cli.core.interactor.properties.reader.PropsFileReader;
import com.webank.wedatasphere.linkis.cli.core.interactor.properties.reader.SysEnvReader;
import com.webank.wedatasphere.linkis.cli.core.interactor.properties.reader.SysPropsReader;
import com.webank.wedatasphere.linkis.cli.core.interactor.validate.ParsedTplValidator;
import com.webank.wedatasphere.linkis.cli.core.interactor.var.StdVarAccess;
import com.webank.wedatasphere.linkis.cli.core.interactor.var.SysVarAccess;
import com.webank.wedatasphere.linkis.cli.core.interactor.var.VarAccess;
import com.webank.wedatasphere.linkis.cli.core.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProcessInputUtil {
    private static Logger logger = LoggerFactory.getLogger(ProcessInputUtil.class);

    public static ProcessedData generateProcessedData(String[] cmdStr, CmdTemplate template) {
    /*
      user input
     */
        Parser parser = new SingleCmdParser()
                .setMapper(null)
                .setTemplate(template)
                .setFitter(new SingleTplFitter());

        ParseResult result = parser.parse(cmdStr);

        ParsedTplValidator parsedTplValidator = new ParsedTplValidator();
        parsedTplValidator.doValidation(result.getParsedTemplateCopy());

        Params params = result.getParams();
        logger.debug("==========params============\n" + Utils.GSON.toJson(params));


        Map<String, ClientProperties> propertiesMap = new HashMap<>();
    /*
      default config, -Dconf.root & -Dconf.file specifies config path
     */
        System.setProperty("conf.root", "src/test/resources/conf/");
        System.setProperty("conf.file", "linkis-cli.properties");
        String configPath = System.getProperty("conf.root");
        String defaultConfFileName = System.getProperty("conf.file");
        List<PropertiesReader> readersList = new PropsFilesScanner().getPropsReaders(configPath); //+1 user config
    /*
      user defined config
     */
        String userConfPath = null;
        if (params.containsParam(LinkisClientKeys.LINKIS_CLIENT_USER_CONFIG)) {
            userConfPath = (String) params
                    .getParamItemMap()
                    .get(LinkisClientKeys.LINKIS_CLIENT_USER_CONFIG)
                    .getValue();
        }
        if (StringUtils.isNotBlank(userConfPath)) {
            PropertiesReader reader = new PropsFileReader()
                    .setPropsId(LinkisClientKeys.LINKIS_CLIENT_USER_CONFIG)
                    .setPropsPath(userConfPath);
            readersList.add(reader);
        } else {
            LogUtils.getInformationLogger().info("User does not provide usr-configuration file. Will use default config");
        }
        readersList.add(new SysPropsReader());
        readersList.add(new SysEnvReader());
        PropertiesLoader loader = new StdPropsLoader()
                .addPropertiesReaders(
                        readersList.toArray(
                                new PropertiesReader[readersList.size()]
                        )
                );
        ClientProperties[] loaderResult = loader.loadProperties();
        for (ClientProperties properties : loaderResult) {
            propertiesMap.put(properties.getPropsId(), properties);
        }

        VarAccess stdVarAccess = new StdVarAccess()
                .setCmdParams(params)
                .setUserConf(propertiesMap.get(LinkisClientKeys.LINKIS_CLIENT_USER_CONFIG))
                .setDefaultConf(propertiesMap.get(AppConstants.DEFAULT_CONFIG_NAME))
                .init();

        VarAccess sysVarAccess = new SysVarAccess()
                .setSysProp(propertiesMap.get(CommonConstants.SYSTEM_PROPERTIES_IDENTIFIER))
                .setSysEnv(propertiesMap.get(CommonConstants.SYSTEM_ENV_IDENTIFIER));

        return new ProcessedData(null, params.getCmdType(), stdVarAccess, sysVarAccess);
    }
}
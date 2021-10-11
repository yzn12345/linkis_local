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

package com.webank.wedatasphere.linkis.cli.core.exception.handler;

import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdTemplate;
import com.webank.wedatasphere.linkis.cli.common.exception.handler.ExceptionHandler;
import com.webank.wedatasphere.linkis.cli.core.data.ClientContext;
import com.webank.wedatasphere.linkis.cli.core.exception.CommandException;
import com.webank.wedatasphere.linkis.cli.core.presenter.HelpInfoPresenter;
import com.webank.wedatasphere.linkis.cli.core.presenter.model.HelpInfoModel;

import java.util.Map;

/**
 * @description: Display help-info if required
 */
public class CommandExceptionHandler implements ExceptionHandler {
    //TODO:move to application
    @Override
    public void handle(Exception e) {
        if (e instanceof CommandException) {
            if (((CommandException) e).requireHelp()) {

                Map<String, CmdTemplate> templateMap = ClientContext.getGeneratedTemplateMap();
                CmdTemplate template = templateMap.get(((CommandException) e).getCmdType().getName());

                if (template != null) {
                    HelpInfoModel model = new HelpInfoModel(template);

                    new HelpInfoPresenter().present(model);
                }
            }
        }
        new DefaultExceptionHandler().handle(e);
    }
}
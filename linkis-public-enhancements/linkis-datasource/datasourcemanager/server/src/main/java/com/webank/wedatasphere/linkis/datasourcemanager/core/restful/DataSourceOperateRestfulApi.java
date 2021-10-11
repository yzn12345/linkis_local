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

package com.webank.wedatasphere.linkis.datasourcemanager.core.restful;

import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSource;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceType;
import com.webank.wedatasphere.linkis.datasourcemanager.core.formdata.FormDataTransformerFactory;
import com.webank.wedatasphere.linkis.datasourcemanager.core.formdata.MultiPartFormDataTransformer;
import com.webank.wedatasphere.linkis.datasourcemanager.core.service.DataSourceInfoService;
import com.webank.wedatasphere.linkis.datasourcemanager.core.service.DataSourceRelateService;
import com.webank.wedatasphere.linkis.datasourcemanager.core.service.MetadataOperateService;
import com.webank.wedatasphere.linkis.datasourcemanager.core.validate.ParameterValidateException;
import com.webank.wedatasphere.linkis.datasourcemanager.core.validate.ParameterValidator;
import com.webank.wedatasphere.linkis.metadatamanager.common.MdmConfiguration;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/data_source/op/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Component
public class DataSourceOperateRestfulApi {

    @Autowired
    private MetadataOperateService metadataOperateService;

    @Autowired
    private DataSourceRelateService dataSourceRelateService;

    @Autowired
    private DataSourceInfoService dataSourceInfoService;

    @Autowired
    private ParameterValidator parameterValidator;

    @Autowired
    private Validator beanValidator;

    private MultiPartFormDataTransformer formDataTransformer;

    @PostConstruct
    public void initRestful(){
        this.formDataTransformer = FormDataTransformerFactory.buildCustom();
    }

    @POST
    @Path("/connect/json")
    public Response connect(DataSource dataSource,
                            @Context HttpServletRequest request){
        return RestfulApiHelper.doAndResponse(() -> {
            String operator = SecurityFilter.getLoginUsername(request);
            //Bean validation
            Set<ConstraintViolation<DataSource>> result = beanValidator.validate(dataSource, Default.class);
            if(result.size() > 0){
                throw new ConstraintViolationException(result);
            }
            doConnect(operator, dataSource);
            return Message.ok().data("ok", true);
        }, "/data_source/op/connect/json","");
    }

    @POST
    @Path("/connect/form")
    public Response connect(FormDataMultiPart multiPartForm,
                            @Context HttpServletRequest request){
        return RestfulApiHelper.doAndResponse(() -> {
            String operator = SecurityFilter.getLoginUsername(request);
            DataSource dataSource = formDataTransformer.transformToObject(multiPartForm, DataSource.class, beanValidator);
            doConnect(operator, dataSource);
            return Message.ok().data("ok", true);
        }, "/data_source/op/connect/form","");
    }

    /**
     * Build a connection
     * @param dataSource
     */
    protected void doConnect(String operator, DataSource dataSource) throws ParameterValidateException {
        if(null != dataSource.getDataSourceEnvId()){
            dataSourceInfoService.addEnvParamsToDataSource(dataSource.getDataSourceEnvId(), dataSource);
        }
        //Validate connect parameters
        List<DataSourceParamKeyDefinition> keyDefinitionList = dataSourceRelateService
                .getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
        dataSource.setKeyDefinitions(keyDefinitionList);
        Map<String,Object> connectParams = dataSource.getConnectParams();
        parameterValidator.validate(keyDefinitionList, connectParams);
        DataSourceType dataSourceType = dataSourceRelateService.getDataSourceType(dataSource.getDataSourceTypeId());
        metadataOperateService.doRemoteConnect(MdmConfiguration.METADATA_SERVICE_APPLICATION.getValue()
                        + (StringUtils.isNotBlank(dataSourceType.getName())?("-" +dataSourceType.getName().toLowerCase()) : ""),
                operator, dataSource.getConnectParams());
    }
}

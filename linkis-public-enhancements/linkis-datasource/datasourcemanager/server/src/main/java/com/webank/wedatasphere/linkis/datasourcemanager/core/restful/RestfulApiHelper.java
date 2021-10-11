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

import com.webank.wedatasphere.linkis.common.exception.WarnException;
import com.webank.wedatasphere.linkis.datasourcemanager.common.DsmConfiguration;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import com.webank.wedatasphere.linkis.datasourcemanager.common.util.CryptoUtils;
import com.webank.wedatasphere.linkis.datasourcemanager.core.restful.exception.BeanValidationExceptionMapper;
import com.webank.wedatasphere.linkis.datasourcemanager.core.validate.ParameterValidateException;
import com.webank.wedatasphere.linkis.server.Message;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Helper of restful api entrance
 */
public class RestfulApiHelper {
    /**
     * If is administrator
     * @param userName user name
     * @return
     */
    public static boolean isAdminUser(String userName){
        List<String> userList = Arrays.asList(DsmConfiguration.DSM_ADMIN_USER_LIST.getValue().split(","));
        return userList.contains(userName);
    }

    /**
     * Encrypt key of password type
     * @param keyDefinitionList definition list
     * @param connectParams connection parameters
     */
    public static void encryptPasswordKey(List<DataSourceParamKeyDefinition> keyDefinitionList,
                                    Map<String, Object> connectParams){
        keyDefinitionList.forEach(keyDefinition -> {
            if(keyDefinition.getValueType() == DataSourceParamKeyDefinition.ValueType.PASSWORD){
                Object password = connectParams.get(keyDefinition.getKey());
                if(null != password){
                    connectParams.put(keyDefinition.getKey(), CryptoUtils.object2String(String.valueOf(password)));
                }
            }
        });
    }

    /**
     * Encrypt key of password type
     * @param keyDefinitionList definition list
     * @param connectParams connection parameters
     */
    public static void decryptPasswordKey(List<DataSourceParamKeyDefinition> keyDefinitionList,
                                          Map<String, Object> connectParams){
        keyDefinitionList.forEach(keyDefinition -> {
            if(keyDefinition.getValueType() == DataSourceParamKeyDefinition.ValueType.PASSWORD){
                Object password = connectParams.get(keyDefinition.getKey());
                if(null != password){
                    connectParams.put(keyDefinition.getKey(), CryptoUtils.string2Object(String.valueOf(password)));
                }
            }
        });
    }

    /**
     *
     * @param tryOperation operate function
     * @param failMessage message
     */
    public static Response doAndResponse(TryOperation tryOperation, String method, String failMessage){
        try{
            Message message = tryOperation.operateAndGetMessage();
            return Message.messageToResponse(setMethod(message, method));
        }catch(ParameterValidateException e){
            return Message.messageToResponse(setMethod(Message.error(e.getMessage()), method));
        }catch(ConstraintViolationException e){
            return new BeanValidationExceptionMapper().toResponse(e);
        }catch(WarnException e){
            return Message.messageToResponse(setMethod(Message.warn(e.getMessage()), method));
        }catch(Exception e){
            return Message.messageToResponse(setMethod(Message.error(failMessage, e), method));
        }
    }
    private static Message setMethod(Message message, String method){
        message.setMethod(method);
        return message;
    }


    @FunctionalInterface
    public interface TryOperation {

        /**
         * Operate method
         */
        Message operateAndGetMessage() throws Exception;
    }
}

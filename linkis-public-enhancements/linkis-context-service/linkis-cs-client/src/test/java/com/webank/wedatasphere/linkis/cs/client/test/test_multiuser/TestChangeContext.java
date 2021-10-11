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

package com.webank.wedatasphere.linkis.cs.client.test.test_multiuser;

import com.google.gson.Gson;
import com.webank.wedatasphere.linkis.cs.client.Context;
import com.webank.wedatasphere.linkis.cs.client.ContextClient;
import com.webank.wedatasphere.linkis.cs.client.builder.ContextClientFactory;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.resource.LinkisBMLResource;
import com.webank.wedatasphere.linkis.cs.common.entity.source.*;
import com.webank.wedatasphere.linkis.cs.common.serialize.helper.ContextSerializationHelper;
import com.webank.wedatasphere.linkis.cs.common.serialize.helper.SerializationHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class TestChangeContext {

    public static void main(String [] args) {

        ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
        try {
            // 1, read context
            File file = new File(TestCreateContext.CONTEXTID_PATH);
            if (!file.exists()) {
                System.out.println("Error, contextID serialize file : " + TestCreateContext.CONTEXTID_PATH + " invalid.");
                return;
            }
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String contextIDStr = null;
            StringBuilder builder = new StringBuilder("");
            java.lang.String tmp = br.readLine();
            while (null != tmp) {
                builder.append(tmp);
                tmp = br.readLine();
            }
            br.close();
            contextIDStr = builder.toString();
            System.out.println("Read contextID : " + contextIDStr);

            SerializationHelper serializationHelper = ContextSerializationHelper.getInstance();
            if (!serializationHelper.accepts(contextIDStr)) {
                System.out.println("Invalid contextStr : " + contextIDStr + ", cannot be deserialized");
                return;
            }
            Object contextObject = serializationHelper.deserialize(contextIDStr);
            System.out.println("Deserialized obj : " + new Gson().toJson(contextObject));
            if (!LinkisHAWorkFlowContextID.class.isInstance(contextObject)) {
                System.out.println("Invalid contextObject, not LinkisHAWorkFlowContextID instance.");
                return;
            }
            LinkisHAWorkFlowContextID contextID = (LinkisHAWorkFlowContextID) contextObject;

            Context context = contextClient.getContext(contextID);
            // 2, update context
            ContextKey contextKey = new CommonContextKey();
            contextKey.setKey("testchange.txt");
            contextKey.setKeywords("xddd");
            contextKey.setContextScope(ContextScope.PUBLIC);
            contextKey.setContextType(ContextType.RESOURCE);
            ContextValue contextValue = new CommonContextValue();
            LinkisBMLResource resource = new LinkisBMLResource();
            resource.setResourceId("456789");
            resource.setVersion("v00001");
            contextValue.setValue(resource);
            ContextKeyValue contextKeyValue = new CommonContextKeyValue();
            contextKeyValue.setContextValue(contextValue);
            contextKeyValue.setContextKey(contextKey);
            context.setContextKeyAndValue(contextKeyValue);

            // 3, get context
            ContextValue contextValueResult = context.getContextValue(contextKey);
            System.out.println("Got contextValue : " + new Gson().toJson(contextValueResult));
            System.out.println("Original contextValue : " + new Gson().toJson(contextValue));

            contextClient.close();
        } catch (Exception e) {
            if (null != contextClient) {
                try {
                    contextClient.close();
                } catch (Exception e1) {

                }
            }
            e.printStackTrace();
        }
    }
}

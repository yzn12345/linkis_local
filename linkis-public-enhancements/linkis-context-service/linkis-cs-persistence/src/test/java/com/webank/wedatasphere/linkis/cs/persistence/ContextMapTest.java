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

package com.webank.wedatasphere.linkis.cs.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.metadata.CSTable;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.LinkisHAWorkFlowContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextID;
import com.webank.wedatasphere.linkis.cs.persistence.persistence.ContextMapPersistence;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Random;

public class ContextMapTest {
    AnnotationConfigApplicationContext context = null;
    ContextMapPersistence contextMapPersistence = null;

    public void before() {
        context = new AnnotationConfigApplicationContext(Scan.class);
        contextMapPersistence = context.getBean(ContextMapPersistence.class);
    }


    public void testcreateContextMap() throws CSErrorException, JsonProcessingException {
        AContextKeyValue aContextKeyValue = new AContextKeyValue();
        PersistenceContextID persistenceContextID = new PersistenceContextID();
        persistenceContextID.setContextId(String.valueOf(new Random().nextInt(100000)));
        AContextValue aContextValue = new AContextValue();
        CSTable csTable = new CSTable();
        csTable.setCreator("neiljianliu");
        csTable.setName("table1");
        aContextValue.setValue(csTable);
        aContextKeyValue.setContextValue(aContextValue);
        aContextKeyValue.setContextKey(new AContextKey());
        contextMapPersistence.create(persistenceContextID, aContextKeyValue);
    }


    public void testDeleteContextID() throws CSErrorException {
        //contextIDPersistence.deleteContextID(-1193959466);
    }


    public void testGetContextMap() throws CSErrorException {
        AContextID aContextID = new AContextID();
        aContextID.setContextId("10067");
        AContextKey aContextKey = new AContextKey();
        aContextKey.setKey("key");
        ContextKeyValue keyValue = contextMapPersistence.get(aContextID, aContextKey);
        System.out.println(((AContextKey)keyValue.getContextKey()).getA());
    }


    public void testGetAllContextMapByKey() throws CSErrorException {
        AContextID aContextID = new AContextID();
        aContextID.setContextId("13532");
        List<ContextKeyValue> key = contextMapPersistence.getAll(aContextID, "ccc");
        System.out.println(key.size());
    }


    public void testGetAllContextMapByContextID() throws CSErrorException {
        AContextID aContextID = new AContextID();
        aContextID.setContextId("82633");
        List<ContextKeyValue> key = contextMapPersistence.getAll(aContextID);
        System.out.println(key.size());
    }


    public void testGetAllContextMapByScope() throws CSErrorException {
        AContextID aContextID = new AContextID();
        aContextID.setContextId("82633");
        List<ContextKeyValue> key = contextMapPersistence.getAll(aContextID,ContextScope.FRIENDLY);
        System.out.println(key.size());
    }


    public void testGetAllContextMapByType() throws CSErrorException {
        AContextID aContextID = new AContextID();
        aContextID.setContextId("82633");
        List<ContextKeyValue> key = contextMapPersistence.getAll(aContextID,ContextType.ENV);
        System.out.println(key.size());
    }


    public void testremoveContextMap() throws CSErrorException {
        AContextID aContextID = new AContextID();
        aContextID.setContextId("13532");
        AContextKey aContextKey = new AContextKey();
        aContextKey.setKey("keyadsfaccc");
        contextMapPersistence.remove(aContextID,aContextKey);
    }


    public void testremoveAllContextMapbyContextID() throws CSErrorException {
        AContextID aContextID = new AContextID();
        aContextID.setContextId("8263356");
/*        AContextKey aContextKey = new AContextKey();
        aContextKey.setKey("keydelete");
        aContextKey.setContextScope(ContextScope.FRIENDLY);
        aContextKey.setContextType(ContextType.ENV);*/
        contextMapPersistence.removeAll(aContextID);
    }


    public void testremoveAllContextMapbyScope() throws CSErrorException {
        AContextID aContextID = new AContextID();
        aContextID.setContextId("82633456");
        contextMapPersistence.removeAll(aContextID,ContextScope.FRIENDLY);
    }


    public void testremoveAllContextMapbyType() throws CSErrorException {
        AContextID aContextID = new AContextID();
        aContextID.setContextId("36694");
        contextMapPersistence.removeAll(aContextID,ContextType.ENV);
    }


    public void testUpdateContextMap() throws CSErrorException, JsonProcessingException {
        AContextKeyValue aContextKeyValue = new AContextKeyValue();
        PersistenceContextID persistenceContextID = new PersistenceContextID();
        persistenceContextID.setContextId("10067");
        AContextValue aContextValue = new AContextValue();
        CSTable csTable = new CSTable();
        csTable.setCreator("johnnwang");
        csTable.setName("tableUpdate");
        aContextValue.setValue(csTable);
        aContextKeyValue.setContextValue(aContextValue);
        AContextKey aContextKey = new AContextKey();
        aContextKey.setKey("key");
        aContextKey.setKeywords("udpate keywords");
        aContextKey.setContextScope(ContextScope.PRIVATE);
        aContextKey.setContextType(ContextType.COST);
        aContextKeyValue.setContextKey(aContextKey);
        contextMapPersistence.update(persistenceContextID,aContextKeyValue);
    }


    public void test003() throws CSErrorException {
        LinkisHAWorkFlowContextID linkisHAWorkFlowContextID = new LinkisHAWorkFlowContextID();
        linkisHAWorkFlowContextID.setContextId("84996");
        List<ContextKeyValue> all = contextMapPersistence.getAll(linkisHAWorkFlowContextID);
        System.out.println(all.size());
    }


    public void test004() throws CSErrorException {
        LinkisHAWorkFlowContextID linkisHAWorkFlowContextID = new LinkisHAWorkFlowContextID();
        linkisHAWorkFlowContextID.setContextId("12345");
        contextMapPersistence.removeByKeyPrefix(linkisHAWorkFlowContextID,ContextType.COST,"aaa");
    }

}

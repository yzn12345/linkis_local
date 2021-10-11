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

package com.webank.wedatasphere.linkis.message;

import com.webank.wedatasphere.linkis.message.annotation.Receiver;
import com.webank.wedatasphere.linkis.message.builder.MessageJob;
import com.webank.wedatasphere.linkis.message.conf.MessageSchedulerConf;
import com.webank.wedatasphere.linkis.message.context.AbstractMessageSchedulerContext;
import com.webank.wedatasphere.linkis.message.context.DefaultMessageSchedulerContext;
import com.webank.wedatasphere.linkis.message.parser.ImplicitMethod;
import com.webank.wedatasphere.linkis.message.parser.ServiceMethod;
import com.webank.wedatasphere.linkis.message.registry.AbstractImplicitRegistry;
import com.webank.wedatasphere.linkis.message.registry.AbstractServiceRegistry;
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol;
import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @date 2020/7/14
 */
public class SchedulerMessageTest {

    private AbstractMessageSchedulerContext context;

    Reflections reflections = new Reflections(MessageSchedulerConf.SERVICE_SCAN_PACKAGE, new MethodAnnotationsScanner());

    @Before
    public void before() {

        context = new DefaultMessageSchedulerContext();
    }


    @Test
    public void servieParserTest() throws InterruptedException {
        Map<String, List<ServiceMethod>> parse = context.getservieParser().parse(new TestService());
        System.out.println(parse.size());
    }

    @Test
    public void registryTest() throws InterruptedException {
        TestService testService = new TestService();
        context.getServiceRegistry().register(testService);
        context.getImplicitRegistry().register(testService);
        System.out.println("serviceRegistry");
    }

    @Test
    public void implicitParserTest() throws InterruptedException {
        Map<String, List<ImplicitMethod>> parse = context.getImplicitParser().parse(new TestService());
        System.out.println(parse.size());
    }

    @Test
    public void springRegisterTest() {
        Set<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(Receiver.class);
        Set<? extends Class<?>> collect = methodsAnnotatedWith.stream().map(Method::getDeclaringClass).collect(Collectors.toSet());
        System.out.println(collect.size());
    }

    @Test
    public void test() {
        System.out.println(RequestProtocol.class.isAssignableFrom(RequestProtocol.class));
    }

    @Test
    public void publishTest() throws InterruptedException, ExecutionException, TimeoutException {
        TestService testService = new TestService();
        TestService2 testService2 = new TestService2();
        AbstractImplicitRegistry implicitRegistry = this.context.getImplicitRegistry();
        implicitRegistry.register(testService);
        implicitRegistry.register(testService2);
        implicitRegistry.register(new ImplicitObject());
        AbstractServiceRegistry serviceRegistry = this.context.getServiceRegistry();
        serviceRegistry.register(testService);
        serviceRegistry.register(testService2);
        long start = System.currentTimeMillis();
        MessageJob publish = context.getPublisher().publish(new DefaultRequestProtocol());
        Object o = publish.get();
        System.out.println(o);

        System.out.println(System.currentTimeMillis() - start);
    }

}

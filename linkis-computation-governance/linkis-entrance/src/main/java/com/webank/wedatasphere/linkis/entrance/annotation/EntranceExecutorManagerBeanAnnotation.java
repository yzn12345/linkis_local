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

package com.webank.wedatasphere.linkis.entrance.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Bean(value = EntranceExecutorManagerBeanAnnotation.BEAN_NAME)
@Component(value = EntranceExecutorManagerBeanAnnotation.BEAN_NAME)
public @interface EntranceExecutorManagerBeanAnnotation {
    String BEAN_NAME = "executorManager";
    @AliasFor(annotation = Component.class)
    String value() default BEAN_NAME;

    @Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier(BEAN_NAME)
    @Autowired
    @interface EntranceExecutorManagerAutowiredAnnotation {
        @AliasFor(annotation = Qualifier.class)
        String value() default BEAN_NAME;
        @AliasFor(annotation = Autowired.class)
        boolean required() default true;
    }
}
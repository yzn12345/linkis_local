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

package com.webank.wedatasphere.linkis;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.common.conf.BDPConfiguration;
import com.webank.wedatasphere.linkis.common.conf.Configuration;
import com.webank.wedatasphere.linkis.common.exception.LinkisException;
import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;
import com.webank.wedatasphere.linkis.server.conf.DataWorkCloudCustomExcludeFilter;
import com.webank.wedatasphere.linkis.server.conf.ServerConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.DispatcherType;
import javax.ws.rs.ext.RuntimeDelegate;
import java.util.EnumSet;


@SpringBootApplication
@EnableDiscoveryClient
@RefreshScope
@ComponentScan(basePackages = "com.webank.wedatasphere",
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = {DataWorkCloudCustomExcludeFilter.class}))
public class DataWorkCloudApplication extends SpringBootServletInitializer {
    private static final Log logger = LogFactory.getLog(DataWorkCloudApplication.class);

    private static ConfigurableApplicationContext applicationContext;
    private static ServiceInstance serviceInstance;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void main(String[] args) throws ReflectiveOperationException {

        RuntimeDelegate.setInstance(new org.glassfish.jersey.internal.RuntimeDelegateImpl());
        final SpringApplication application = new SpringApplication(DataWorkCloudApplication.class);
        application.addListeners(new ApplicationListener<ApplicationPreparedEvent>(){
            public void onApplicationEvent(ApplicationPreparedEvent applicationPreparedEvent) {
                logger.info("add config from config server...");
                if(applicationContext == null) {
                    applicationContext = applicationPreparedEvent.getApplicationContext();
                }
                addRemoteConfig();
                logger.info("initialize DataWorkCloud spring application...");
                initDWCApplication();
            }
        });
        application.addListeners(new ApplicationListener<RefreshScopeRefreshedEvent>() {
            public void onApplicationEvent(RefreshScopeRefreshedEvent applicationEvent) {
                logger.info("refresh config from config server...");
                updateRemoteConfig();
            }
        });
        String listeners = ServerConfiguration.BDP_SERVER_SPRING_APPLICATION_LISTENERS().getValue();
        if(StringUtils.isNotBlank(listeners)) {
            for (String listener : listeners.split(",")) {
                application.addListeners((ApplicationListener<?>) Class.forName(listener).newInstance());
            }
        }
        if("true".equals(ServerConfiguration.IS_GATEWAY().getValue())){
            application.setWebApplicationType(WebApplicationType.REACTIVE);
        }
        applicationContext = application.run(args);
    }

    public static void updateRemoteConfig() {
        addOrUpdateRemoteConfig(applicationContext.getEnvironment(), true);
    }

    public static void addRemoteConfig() {
        addOrUpdateRemoteConfig(applicationContext.getEnvironment(), false);
    }

    private static void addOrUpdateRemoteConfig(Environment env, boolean isUpdateOrNot) {
        StandardEnvironment environment = (StandardEnvironment) env;
        PropertySource propertySource = environment.getPropertySources().get("bootstrapProperties");
        if(propertySource == null) {
            return;
        }
        CompositePropertySource source = (CompositePropertySource) propertySource;
        for (String key: source.getPropertyNames()) {
            Object val = source.getProperty(key);
            if(val == null) {
                continue;
            }
            if(isUpdateOrNot) {
                logger.info("update remote config => " + key + " = " + source.getProperty(key));
                BDPConfiguration.set(key, val.toString());
            } else {
                logger.info("add remote config => " + key + " = " + source.getProperty(key));
                BDPConfiguration.setIfNotExists(key, val.toString());
            }
        }
    }

    private static void initDWCApplication() {
        String hostName = Utils.getComputerName();
        boolean eurekaPreferIp = Configuration.EUREKA_PREFER_IP();
        if(eurekaPreferIp){
            hostName = applicationContext.getEnvironment().getProperty("spring.cloud.client.ip-address");
            logger.info("using ip address replace hostname,beacause eureka.instance.prefer-ip-address:" + eurekaPreferIp);
        }
        serviceInstance = new ServiceInstance();
        serviceInstance.setApplicationName(applicationContext.getEnvironment().getProperty("spring.application.name"));
        serviceInstance.setInstance(hostName + ":" + applicationContext.getEnvironment().getProperty("server.port"));
        LinkisException.setApplicationName(serviceInstance.getApplicationName());
        LinkisException.setHostname(hostName);
        LinkisException.setHostPort(Integer.parseInt(applicationContext.getEnvironment().getProperty("server.port")));
    }

    public static ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public static String getApplicationName() {
      return serviceInstance.getApplicationName();
    }

    public static String getInstance() {
        return serviceInstance.getInstance();
    }

    public static void setProperty(String key, String value) {
        BDPConfiguration.set(key, value);
    }

    public static void setPropertyIfNotExists(String key, String value) {
        BDPConfiguration.setIfNotExists(key, value);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(DataWorkCloudApplication.class);
    }

    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> jettyFactoryCustomizer() {
        return new WebServerFactoryCustomizer<JettyServletWebServerFactory>() {
            public void customize(JettyServletWebServerFactory jettyServletWebServerFactory) {
                jettyServletWebServerFactory.addServerCustomizers(new JettyServerCustomizer() {
                    public void customize(Server server) {
                        Handler[] childHandlersByClass = server.getChildHandlersByClass(WebAppContext.class);
                        final WebAppContext webApp = (WebAppContext) childHandlersByClass[0];
                        FilterHolder filterHolder = new FilterHolder(CharacterEncodingFilter.class);
                        filterHolder.setInitParameter("encoding", Configuration.BDP_ENCODING().getValue());
                        filterHolder.setInitParameter("forceEncoding", "true");
                        webApp.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));
                        BDPJettyServerHelper.setupRestApiContextHandler(webApp);
                        if(ServerConfiguration.BDP_SERVER_SOCKET_MODE().getValue()) {
                            BDPJettyServerHelper.setupControllerServer(webApp);
                        }
                        if(!ServerConfiguration.BDP_SERVER_DISTINCT_MODE().getValue()) {
                            BDPJettyServerHelper.setupWebAppContext(webApp);
                        }
                    }
                });
            }
        };
    }
}

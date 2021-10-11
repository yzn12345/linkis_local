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

package com.webank.wedatasphere.linkis.gateway.springcloud.http;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import com.webank.wedatasphere.linkis.common.utils.JavaLog;
import com.webank.wedatasphere.linkis.gateway.exception.GatewayWarnException;
import com.webank.wedatasphere.linkis.gateway.http.BaseGatewayContext;
import com.webank.wedatasphere.linkis.gateway.parser.GatewayParser;
import com.webank.wedatasphere.linkis.gateway.route.GatewayRouter;
import com.webank.wedatasphere.linkis.gateway.security.LinkisPreFilter;
import com.webank.wedatasphere.linkis.gateway.security.LinkisPreFilter$;
import com.webank.wedatasphere.linkis.gateway.security.SecurityFilter;
import com.webank.wedatasphere.linkis.gateway.springcloud.SpringCloudGatewayConfiguration;
import com.webank.wedatasphere.linkis.server.Message;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.support.DefaultServerRequest;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.codec.AbstractDataBufferDecoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.http.server.reactive.AbstractServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class GatewayAuthorizationFilter extends JavaLog implements GlobalFilter, Ordered {

    private GatewayParser parser;
    private GatewayRouter router;
    private GatewayProperties gatewayProperties;

    private final Integer MAX_BUFFER_SIZE = CommonVars.apply("wds.linkis.gateway.max.buffer.size", 128 * 1024 * 1024).getValue();

    private List<LinkisPreFilter> linkisPreFilters = LinkisPreFilter$.MODULE$.getLinkisPreFilters();

    public GatewayAuthorizationFilter(GatewayParser parser, GatewayRouter router, GatewayProperties gatewayProperties) {
        this.parser = parser;
        this.router = router;
        this.gatewayProperties = gatewayProperties;
    }

    private String getRequestBody(ServerWebExchange exchange) {
//        StringBuilder requestBody = new StringBuilder();
        DefaultServerRequest serverRequest = new DefaultServerRequest(exchange);
        String requestBody = null;
        try {
            requestBody = serverRequest.bodyToMono(String.class).toFuture().get();
        } catch (Exception e) {
            GatewayWarnException exception = new GatewayWarnException(18000, "get requestBody failed!");
            exception.initCause(e);
            throw exception;
        }
        return requestBody;
    }

    private BaseGatewayContext getBaseGatewayContext(ServerWebExchange exchange, Route route) {
        AbstractServerHttpRequest request = (AbstractServerHttpRequest) exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        BaseGatewayContext gatewayContext = new BaseGatewayContext();
        SpringCloudGatewayHttpRequest springCloudGatewayHttpRequest = new SpringCloudGatewayHttpRequest(request);
        gatewayContext.setRequest(springCloudGatewayHttpRequest);
        gatewayContext.setResponse(new SpringCloudGatewayHttpResponse(response));
        if(route.getUri().toString().startsWith(SpringCloudGatewayConfiguration.ROUTE_URI_FOR_WEB_SOCKET_HEADER())){
            gatewayContext.setWebSocketRequest();
        }
//        if(!gatewayContext.isWebSocketRequest() && parser.shouldContainRequestBody(gatewayContext)) {
//            String requestBody = getRequestBody(exchange);
//            springCloudGatewayHttpRequest.setRequestBody(requestBody);
//        }
        return gatewayContext;
    }

    private Route getRealRoute(Route route, ServiceInstance serviceInstance) {
        String routeUri = route.getUri().toString();
        String scheme = route.getUri().getScheme();
        if(routeUri.startsWith(SpringCloudGatewayConfiguration.ROUTE_URI_FOR_WEB_SOCKET_HEADER())) {
            scheme = SpringCloudGatewayConfiguration.ROUTE_URI_FOR_WEB_SOCKET_HEADER();
        } else if(routeUri.startsWith(SpringCloudGatewayConfiguration.ROUTE_URI_FOR_HTTP_HEADER())) {
            scheme = SpringCloudGatewayConfiguration.ROUTE_URI_FOR_HTTP_HEADER();
        } else {
            scheme += "://";
        }
        String uri = scheme + serviceInstance.getApplicationName();
        if(StringUtils.isNotBlank(serviceInstance.getInstance())) {
            uri = scheme + SpringCloudGatewayConfiguration.mergeServiceInstance(serviceInstance);
        }
        return Route.async().id(route.getId()).filters(route.getFilters()).order(route.getOrder())
                .uri(uri).asyncPredicate(route.getPredicate()).build();
    }
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        AbstractServerHttpRequest request = (AbstractServerHttpRequest) exchange.getRequest();
//        ServerHttpResponse response = exchange.getResponse();
//        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
//        BaseGatewayContext gatewayContext = getBaseGatewayContext(exchange, route);
//
//        DataBufferFactory bufferFactory = response.bufferFactory();
//        if(((SpringCloudGatewayHttpRequest)gatewayContext.getRequest()).isRequestBodyAutowired()) {
//            ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(request) {
//                @Override
//                public Flux<DataBuffer> getBody() {
//                    if(StringUtils.isBlank(gatewayContext.getRequest().getRequestBody())) return Flux.empty();
//                    return Flux.just(bufferFactory.wrap(gatewayContext.getRequest().getRequestBody().getBytes(StandardCharsets.UTF_8)));
//                }
//            };
//            return chain.filter(exchange.mutate().request(decorator).build());
//        } else {
//            return chain.filter(exchange);
//        }
//    }

    private Mono<Void> gatewayDeal(ServerWebExchange exchange, GatewayFilterChain chain, BaseGatewayContext gatewayContext) {
        SpringCloudGatewayHttpResponse gatewayHttpResponse = (SpringCloudGatewayHttpResponse) gatewayContext.getResponse();

        if(!SecurityFilter.doFilter(gatewayContext)) {
            return gatewayHttpResponse.getResponseMono();
        } else if(gatewayContext.isWebSocketRequest()) {
            return chain.filter(exchange);
        }

        for (LinkisPreFilter linkisPreFilter : linkisPreFilters){
            if (! linkisPreFilter.doFilter(gatewayContext)) {
                return gatewayHttpResponse.getResponseMono();
            }
        }

        ServiceInstance serviceInstance;
        try {
            parser.parse(gatewayContext);
            if(gatewayHttpResponse.isCommitted()) {
                return gatewayHttpResponse.getResponseMono();
            }
            serviceInstance = router.route(gatewayContext);
        } catch (Throwable t) {
            warn("", t);
            Message message = Message.error(t)
                    .$less$less(gatewayContext.getRequest().getRequestURI());
            if (!gatewayContext.isWebSocketRequest()) {
                gatewayHttpResponse.write(Message.response(message));
            } else {
                gatewayHttpResponse.writeWebSocket(Message.response(message));
            }
            gatewayHttpResponse.sendResponse();
            return gatewayHttpResponse.getResponseMono();
        }
        if(gatewayHttpResponse.isCommitted()) {
            return gatewayHttpResponse.getResponseMono();
        }
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if(serviceInstance != null) {
            Route realRoute = getRealRoute(route, serviceInstance);
            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, realRoute);
        } else {
            RouteDefinition realRd = null;
            String proxyId = gatewayContext.getGatewayRoute().getParams().get("proxyId");
            for(RouteDefinition rd : gatewayProperties.getRoutes()){
                if((realRd == null && rd.getId().equals("dws")) ||
                        (rd.getId().equals(proxyId))){
                    realRd = rd;
                }
            }
            String uri = realRd.getUri().toString();
            if(uri != null){
                uri = uri + StringUtils.replace(exchange.getRequest().getPath().value(), "/" + realRd.getId() + "/", "");
                info("Proxy to " + uri);
                Route realRoute = Route.async().id(route.getId()).filters(route.getFilters()).order(route.getOrder())
                        .uri(uri).asyncPredicate(route.getPredicate()).build();
                exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, realRoute);
            }
        }
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate().headers(SpringCloudHttpUtils::addIgnoreTimeoutSignal);
        if(!((SpringCloudGatewayHttpRequest) gatewayContext.getRequest()).getAddCookies().isEmpty()) {
            builder.headers(httpHeaders -> {
                SpringCloudHttpUtils.addCookies(httpHeaders, ((SpringCloudGatewayHttpRequest) gatewayContext.getRequest()).getAddCookies());
            });
        }
        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        AbstractServerHttpRequest request = (AbstractServerHttpRequest) exchange.getRequest();
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        BaseGatewayContext gatewayContext = getBaseGatewayContext(exchange, route);
        if(!gatewayContext.isWebSocketRequest() && parser.shouldContainRequestBody(gatewayContext)) {
            DefaultServerRequest defaultServerRequest = new DefaultServerRequest(exchange);
            defaultServerRequest.messageReaders().stream().filter(reader -> reader instanceof DecoderHttpMessageReader)
                    .filter(httpMessageReader -> ((DecoderHttpMessageReader<?>) httpMessageReader).getDecoder() instanceof AbstractDataBufferDecoder)
                    .forEach(httpMessageReader -> ((AbstractDataBufferDecoder<?>) ((DecoderHttpMessageReader<?>) httpMessageReader).getDecoder()).setMaxInMemorySize(MAX_BUFFER_SIZE));
            return defaultServerRequest.bodyToMono(String.class).flatMap(requestBody -> {
                ((SpringCloudGatewayHttpRequest) gatewayContext.getRequest()).setRequestBody(requestBody);
                ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(request) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        if (StringUtils.isBlank(requestBody)) {
                            return Flux.empty();
                        }
                        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                        return Flux.just(bufferFactory.wrap(requestBody.getBytes(StandardCharsets.UTF_8)));
                    }
                };
                return gatewayDeal(exchange.mutate().request(decorator).build(), chain, gatewayContext);
            });
        } else {
            return gatewayDeal(exchange, chain, gatewayContext);
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}

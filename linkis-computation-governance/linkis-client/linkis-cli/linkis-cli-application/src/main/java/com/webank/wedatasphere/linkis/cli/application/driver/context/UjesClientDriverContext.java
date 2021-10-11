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

package com.webank.wedatasphere.linkis.cli.application.driver.context;

public class UjesClientDriverContext implements LinkisClientDriverContext {
    private String gatewayUrl;
    private Long connectionTimeout;
    private Boolean discoveryEnabled;
    private Boolean loadbalancerEnabled;
    private Long readTimeoutMills;

    private String tokenKey;
    private String tokenValue;

    private String authenticationStrategyStr;
    private Long discoveryFrequencyMills;
    private Integer maxConnectionSize;
    private Boolean retryEnabled;
    private String dwsVersion;

    public UjesClientDriverContext() {
    }

    @Override
    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Boolean getDiscoveryEnabled() {
        return discoveryEnabled;
    }

    public void setDiscoveryEnabled(Boolean discoveryEnabled) {
        this.discoveryEnabled = discoveryEnabled;
    }

    public Boolean getLoadbalancerEnabled() {
        return loadbalancerEnabled;
    }

    public void setLoadbalancerEnabled(Boolean loadbalancerEnabled) {
        this.loadbalancerEnabled = loadbalancerEnabled;
    }

    public Long getReadTimeoutMills() {
        return readTimeoutMills;
    }

    public void setReadTimeoutMills(Long readTimeoutMills) {
        this.readTimeoutMills = readTimeoutMills;
    }

    public String getTokenKey() {
        return tokenKey;
    }

    public void setTokenKey(String tokenKey) {
        this.tokenKey = tokenKey;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public String getAuthenticationStrategyStr() {
        return authenticationStrategyStr;
    }

    public void setAuthenticationStrategyStr(String authenticationStrategyStr) {
        this.authenticationStrategyStr = authenticationStrategyStr;
    }

    public Long getDiscoveryFrequencyMills() {
        return discoveryFrequencyMills;
    }

    public void setDiscoveryFrequencyMills(Long discoveryFrequencyMills) {
        this.discoveryFrequencyMills = discoveryFrequencyMills;
    }

    public Integer getMaxConnectionSize() {
        return maxConnectionSize;
    }

    public void setMaxConnectionSize(Integer maxConnectionSize) {
        this.maxConnectionSize = maxConnectionSize;
    }

    public Boolean getRetryEnabled() {
        return retryEnabled;
    }

    public void setRetryEnabled(Boolean retryEnabled) {
        this.retryEnabled = retryEnabled;
    }

    public String getDwsVersion() {
        return dwsVersion;
    }

    public void setDwsVersion(String dwsVersion) {
        this.dwsVersion = dwsVersion;
    }

}
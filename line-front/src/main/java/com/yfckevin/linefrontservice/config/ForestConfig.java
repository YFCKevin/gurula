package com.yfckevin.linefrontservice.config;

import com.dtflys.forest.config.ForestConfiguration;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.interceptor.Interceptor;
import com.yfckevin.common.utils.MemberContext;
import com.yfckevin.linefrontservice.ConfigProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class ForestConfig {
    private final ConfigProperties configProperties;

    public ForestConfig(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Bean(name = "customForestConfiguration")
    public ForestConfiguration forestConfiguration() {
        ForestConfiguration configuration = ForestConfiguration.configuration();
        configuration.setVariableValue("backendDomain", configProperties.getBackendDomain());
        return configuration;
    }
}

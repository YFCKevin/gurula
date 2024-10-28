package com.yfckevin.linefrontservice.config;

import com.dtflys.forest.config.ForestConfiguration;
import com.yfckevin.api.badminton.ConfigProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ForestConfig {
    private final ConfigProperties configProperties;
    private final LoadBalancerClient loadBalancerClient;

    public ForestConfig(ConfigProperties configProperties, LoadBalancerClient loadBalancerClient) {
        this.configProperties = configProperties;
        this.loadBalancerClient = loadBalancerClient;
    }

    @Bean(name = "customForestConfiguration")
    public ForestConfiguration forestConfiguration() {
        ForestConfiguration configuration = ForestConfiguration.configuration();
        ServiceInstance instance = loadBalancerClient.choose("lineservice");
        if (instance == null) {
            configuration.setVariableValue("backendDomain", configProperties.getBackendDomain());
        } else {
            configuration.setVariableValue("backendDomain", instance.getUri().toString());
        }
        return configuration;
    }
}

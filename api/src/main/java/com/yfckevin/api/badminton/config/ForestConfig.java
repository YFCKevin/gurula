package com.yfckevin.api.badminton.config;

import com.dtflys.forest.config.ForestConfiguration;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.interceptor.Interceptor;
import com.yfckevin.api.badminton.ConfigProperties;
import com.yfckevin.common.utils.MemberContext;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

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
        ServiceInstance instance = loadBalancerClient.choose("badmintonservice");
        if (instance == null) {
            System.out.println(123);
            configuration.setVariableValue("backendDomain", configProperties.getBackendDomain());
        } else {
            System.out.println(456);
            System.out.println(instance.getUri().toString());
            configuration.setVariableValue("backendDomain", instance.getUri().toString() + "/badminton/");
        }

        List<Class<? extends Interceptor>> interceptors = Collections.singletonList(MemberInfoInterceptor.class);
        configuration.setInterceptors(interceptors);

        return configuration;
    }

    public static class MemberInfoInterceptor<T> implements Interceptor<T> {
        @Override
        public boolean beforeExecute(ForestRequest request) {
            String memberInfo = MemberContext.getMember();
            if (memberInfo != null) {
                request.addHeader("member-info", memberInfo);
            }
            return true;
        }
    }
}

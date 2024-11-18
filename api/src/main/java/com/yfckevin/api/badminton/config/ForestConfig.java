package com.yfckevin.api.badminton.config;

import com.dtflys.forest.config.ForestConfiguration;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.interceptor.Interceptor;
import com.yfckevin.api.badminton.ConfigProperties;
import com.yfckevin.api.badminton.loadBalancer.LoadBalancerFactory;
import com.yfckevin.common.utils.MemberContext;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class ForestConfig {
    private final ConfigProperties configProperties;
    private final LoadBalancerFactory loadBalancerFactory;

    public ForestConfig(ConfigProperties configProperties, LoadBalancerFactory loadBalancerFactory) {
        this.configProperties = configProperties;
        this.loadBalancerFactory = loadBalancerFactory;
    }

    @Bean(name = "customForestConfiguration")
    public ForestConfiguration forestConfiguration() {
        ForestConfiguration configuration = ForestConfiguration.configuration();

        loadBalancerFactory.getStrategy("badmintonservice").setForestBasedDomain(configuration, configProperties);
        loadBalancerFactory.getStrategy("cms").setForestBasedDomain(configuration, configProperties);

        List<Class<? extends Interceptor>> interceptors = Collections.singletonList(MemberInfoInterceptor.class);
        configuration.setInterceptors(interceptors);

        // 設定超時
        configuration.setConnectTimeout(5000); // 連接超時 (毫秒)
        configuration.setReadTimeout(10000);  // 讀取超時 (毫秒)

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

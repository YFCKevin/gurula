package com.yfckevin.api.badminton.loadBalancer;

import com.dtflys.forest.config.ForestConfiguration;
import com.yfckevin.api.badminton.ConfigProperties;
import org.springframework.cloud.client.ServiceInstance;

public interface LoadBalancerStrategy {
    ServiceInstance chooseInstance();
    void setForestBasedDomain(ForestConfiguration configuration, ConfigProperties configProperties);
}

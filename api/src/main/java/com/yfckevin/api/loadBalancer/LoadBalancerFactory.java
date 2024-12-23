package com.yfckevin.api.loadBalancer;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoadBalancerFactory {
    private final Map<String, LoadBalancerStrategy> strategies = new HashMap<>();

    public LoadBalancerFactory(LoadBalancerClient loadBalancerClient) {
        strategies.put("badmintonservice", new BadmintonServiceStrategy(loadBalancerClient));
        strategies.put("cms", new CmsServiceStrategy(loadBalancerClient));
        strategies.put("memberservice", new MemberServiceStrategy(loadBalancerClient));
        strategies.put("lineservice", new LineServiceStrategy(loadBalancerClient));
    }

    public LoadBalancerStrategy getStrategy(String serviceDomain) {
        return strategies.get(serviceDomain);
    }
}

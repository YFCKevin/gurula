package com.yfckevin.api.loadBalancer;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

public class LineServiceStrategy extends AbstractLoadBalancerStrategy{
    public LineServiceStrategy(LoadBalancerClient loadBalancerClient) {
        super(loadBalancerClient, "lineservice", "lineServiceDomain", "/line/");

    }
}

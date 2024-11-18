package com.yfckevin.api.badminton.loadBalancer;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

public class BadmintonServiceStrategy extends AbstractLoadBalancerStrategy{
    public BadmintonServiceStrategy(LoadBalancerClient loadBalancerClient) {
        super(loadBalancerClient, "badmintonservice", "badmintonDomain", "/badminton/");
    }
}

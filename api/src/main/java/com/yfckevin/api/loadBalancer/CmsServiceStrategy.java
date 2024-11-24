package com.yfckevin.api.loadBalancer;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

public class CmsServiceStrategy extends AbstractLoadBalancerStrategy{
    public CmsServiceStrategy(LoadBalancerClient loadBalancerClient) {
        super(loadBalancerClient, "cms", "cmsDomain", "/cms/");
    }
}

package com.yfckevin.api.loadBalancer;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

public class MemberServiceStrategy extends AbstractLoadBalancerStrategy{
    public MemberServiceStrategy(LoadBalancerClient loadBalancerClient) {
        super(loadBalancerClient, "memberservice", "memberDomain", "/member/");

    }
}

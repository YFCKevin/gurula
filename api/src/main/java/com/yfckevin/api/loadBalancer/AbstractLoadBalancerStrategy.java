package com.yfckevin.api.loadBalancer;

import com.dtflys.forest.config.ForestConfiguration;
import com.yfckevin.api.ConfigProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

public class AbstractLoadBalancerStrategy implements LoadBalancerStrategy{
    private final LoadBalancerClient loadBalancerClient;
    private final String serviceName;
    private final String serviceDomain;
    private final String contextPath;

    public AbstractLoadBalancerStrategy(LoadBalancerClient loadBalancerClient, String serviceName, String serviceDomain, String contextPath) {
        this.loadBalancerClient = loadBalancerClient;
        this.serviceName = serviceName;
        this.serviceDomain = serviceDomain;
        this.contextPath = contextPath;
    }


    @Override
    public ServiceInstance chooseInstance() {
        return loadBalancerClient.choose(serviceName);
    }

    @Override
    public void setForestBasedDomain(ForestConfiguration configuration, ConfigProperties configProperties) {
        final ServiceInstance instance = chooseInstance();

        if (instance != null) {
            configuration.setVariableValue(serviceDomain, instance.getUri().toString() + contextPath);
            System.out.println(instance.getUri().toString() + contextPath);
        } else {
            switch (serviceName) {
                case "badmintonservice":
                    configuration.setVariableValue(serviceDomain, configProperties.getBadmintonDomain());
                    break;
                case "cms":
                    configuration.setVariableValue(serviceDomain, configProperties.getCmsDomain());
                    System.out.println(configProperties.getCmsDomain());
                    break;
            }
        }
    }
}

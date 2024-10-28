package com.yfckevin.api.badminton;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public class ConfigProperties {
    private String backendDomain;

    public String getBackendDomain() {
        return backendDomain;
    }

    public void setBackendDomain(String backendDomain) {
        this.backendDomain = backendDomain;
    }
}

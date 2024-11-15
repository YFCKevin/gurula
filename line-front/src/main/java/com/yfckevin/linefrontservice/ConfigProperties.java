package com.yfckevin.linefrontservice;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public class ConfigProperties {
    private String backendDomain;
    private String backendLoginDomain;
    private String globalDomain;
    private String badmintonFrontDomain;

    public String getBadmintonFrontDomain() {
        return badmintonFrontDomain;
    }

    public void setBadmintonFrontDomain(String badmintonFrontDomain) {
        this.badmintonFrontDomain = badmintonFrontDomain;
    }

    public String getGlobalDomain() {
        return globalDomain;
    }

    public void setGlobalDomain(String globalDomain) {
        this.globalDomain = globalDomain;
    }

    public String getBackendDomain() {
        return backendDomain;
    }

    public void setBackendDomain(String backendDomain) {
        this.backendDomain = backendDomain;
    }

    public String getBackendLoginDomain() {
        return backendLoginDomain;
    }

    public void setBackendLoginDomain(String backendLoginDomain) {
        this.backendLoginDomain = backendLoginDomain;
    }
}

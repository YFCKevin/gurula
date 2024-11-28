package com.yfckevin.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public class ConfigProperties {
    private String badmintonDomain;
    private String cmsDomain;
    private String lineServiceDomain;

    public String getLineServiceDomain() {
        return lineServiceDomain;
    }

    public void setLineServiceDomain(String lineServiceDomain) {
        this.lineServiceDomain = lineServiceDomain;
    }

    public String getBadmintonDomain() {
        return badmintonDomain;
    }

    public void setBadmintonDomain(String badmintonDomain) {
        this.badmintonDomain = badmintonDomain;
    }

    public String getCmsDomain() {
        return cmsDomain;
    }

    public void setCmsDomain(String cmsDomain) {
        this.cmsDomain = cmsDomain;
    }
}

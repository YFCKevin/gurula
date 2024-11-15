package com.yfckevin.badmintonfront;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@ConfigurationProperties("config")
public class ConfigProperties {
    private String fileSavePath;
    private String lineDomain;
    private String backendLoginDomain;

    public String getBackendLoginDomain() {
        return backendLoginDomain;
    }

    public void setBackendLoginDomain(String backendLoginDomain) {
        this.backendLoginDomain = backendLoginDomain;
    }

    public String getFileSavePath() {
        return fileSavePath;
    }

    public void setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath;
    }

    public String getLineDomain() {
        return lineDomain;
    }

    public void setLineDomain(String lineDomain) {
        this.lineDomain = lineDomain;
    }
}

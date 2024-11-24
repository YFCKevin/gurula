package com.yfckevin.memberservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public class ConfigProperties {
    private String mongodbUri;
    private String globalDomain;
    private String badmintonDomain;
    private String inkCloudDomain;
    private String bingBaoDomain;

    public String getBadmintonDomain() {
        return badmintonDomain;
    }

    public void setBadmintonDomain(String badmintonDomain) {
        this.badmintonDomain = badmintonDomain;
    }

    public String getInkCloudDomain() {
        return inkCloudDomain;
    }

    public void setInkCloudDomain(String inkCloudDomain) {
        this.inkCloudDomain = inkCloudDomain;
    }

    public String getBingBaoDomain() {
        return bingBaoDomain;
    }

    public void setBingBaoDomain(String bingBaoDomain) {
        this.bingBaoDomain = bingBaoDomain;
    }

    public String getGlobalDomain() {
        return globalDomain;
    }

    public void setGlobalDomain(String globalDomain) {
        this.globalDomain = globalDomain;
    }

    public String getMongodbUri() {
        return mongodbUri;
    }

    public void setMongodbUri(String mongodbUri) {
        this.mongodbUri = mongodbUri;
    }
}

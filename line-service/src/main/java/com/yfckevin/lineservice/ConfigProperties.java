package com.yfckevin.lineservice;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public class ConfigProperties {
    private String channelAccessToken;
    private String mongodbUri;
    private String badmintonDomain;
    private String bingBaoDomain;
    private String inkCloudDomain;
    private String picShowPath;
    private String picSavePath;

    public String getPicShowPath() {
        return picShowPath;
    }

    public void setPicShowPath(String picShowPath) {
        this.picShowPath = picShowPath;
    }

    public String getPicSavePath() {
        return picSavePath;
    }

    public void setPicSavePath(String picSavePath) {
        this.picSavePath = picSavePath;
    }

    public String getBadmintonDomain() {
        return badmintonDomain;
    }

    public void setBadmintonDomain(String badmintonDomain) {
        this.badmintonDomain = badmintonDomain;
    }

    public String getBingBaoDomain() {
        return bingBaoDomain;
    }

    public void setBingBaoDomain(String bingBaoDomain) {
        this.bingBaoDomain = bingBaoDomain;
    }

    public String getInkCloudDomain() {
        return inkCloudDomain;
    }

    public void setInkCloudDomain(String inkCloudDomain) {
        this.inkCloudDomain = inkCloudDomain;
    }

    public String getChannelAccessToken() {
        return channelAccessToken;
    }

    public void setChannelAccessToken(String channelAccessToken) {
        this.channelAccessToken = channelAccessToken;
    }

    public String getMongodbUri() {
        return mongodbUri;
    }

    public void setMongodbUri(String mongodbUri) {
        this.mongodbUri = mongodbUri;
    }
}

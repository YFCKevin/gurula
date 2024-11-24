package com.yfckevin.cms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public class ConfigProperties {
    private String aiPicSavePath;
    private String visionAIJsonPath;
    private String openAIApiKey;
    private String mongodbUri;
    private String videoShowPath;
    private String audioSavePath;
    private String picSavePath;
    private String videoSavePath;
    private String rabbitmqHost;
    private String rabbitmqUserName;
    private String rabbitmqPassword;

    public String getVideoShowPath() {
        return videoShowPath;
    }

    public void setVideoShowPath(String videoShowPath) {
        this.videoShowPath = videoShowPath;
    }

    public String getMongodbUri() {
        return mongodbUri;
    }

    public void setMongodbUri(String mongodbUri) {
        this.mongodbUri = mongodbUri;
    }

    public String getOpenAIApiKey() {
        return openAIApiKey;
    }

    public void setOpenAIApiKey(String openAIApiKey) {
        this.openAIApiKey = openAIApiKey;
    }

    public String getVisionAIJsonPath() {
        return visionAIJsonPath;
    }

    public void setVisionAIJsonPath(String visionAIJsonPath) {
        this.visionAIJsonPath = visionAIJsonPath;
    }

    public String getAiPicSavePath() {
        return aiPicSavePath;
    }

    public void setAiPicSavePath(String aiPicSavePath) {
        this.aiPicSavePath = aiPicSavePath;
    }

    public String getRabbitmqHost() {
        return rabbitmqHost;
    }

    public void setRabbitmqHost(String rabbitmqHost) {
        this.rabbitmqHost = rabbitmqHost;
    }

    public String getRabbitmqUserName() {
        return rabbitmqUserName;
    }

    public void setRabbitmqUserName(String rabbitmqUserName) {
        this.rabbitmqUserName = rabbitmqUserName;
    }

    public String getRabbitmqPassword() {
        return rabbitmqPassword;
    }

    public void setRabbitmqPassword(String rabbitmqPassword) {
        this.rabbitmqPassword = rabbitmqPassword;
    }

    public String getAudioSavePath() {
        return audioSavePath;
    }

    public void setAudioSavePath(String audioSavePath) {
        this.audioSavePath = audioSavePath;
    }

    public String getPicSavePath() {
        return picSavePath;
    }

    public void setPicSavePath(String picSavePath) {
        this.picSavePath = picSavePath;
    }

    public String getVideoSavePath() {
        return videoSavePath;
    }

    public void setVideoSavePath(String videoSavePath) {
        this.videoSavePath = videoSavePath;
    }
}

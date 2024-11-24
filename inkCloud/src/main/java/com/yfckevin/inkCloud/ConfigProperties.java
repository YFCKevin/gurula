package com.yfckevin.inkCloud;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public class ConfigProperties {
    private String backendDomain;
    private String backendLoginDomain;
    private String globalDomain;
    private String badmintonFrontDomain;
    private String audioSavePath;
    private String picSavePath;
    private String aiPicSavePath;
    private String videoSavePath;

    public String getVideoSavePath() {
        return videoSavePath;
    }

    public void setVideoSavePath(String videoSavePath) {
        this.videoSavePath = videoSavePath;
    }

    public String getAiPicSavePath() {
        return aiPicSavePath;
    }

    public void setAiPicSavePath(String aiPicSavePath) {
        this.aiPicSavePath = aiPicSavePath;
    }

    public String getPicSavePath() {
        return picSavePath;
    }

    public void setPicSavePath(String picSavePath) {
        this.picSavePath = picSavePath;
    }

    public String getAudioSavePath() {
        return audioSavePath;
    }

    public void setAudioSavePath(String audioSavePath) {
        this.audioSavePath = audioSavePath;
    }

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

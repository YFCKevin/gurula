package com.yfckevin.inkCloud;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public class ConfigProperties {
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
}

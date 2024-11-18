package com.yfckevin.cms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public class ConfigProperties {
    private String aiPicSavePath;
    private String visionAIJsonPath;
    private String openAIApiKey;
    private String mongodbUri;

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
}

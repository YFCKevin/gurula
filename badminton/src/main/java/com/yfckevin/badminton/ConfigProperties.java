package com.yfckevin.badminton;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public class ConfigProperties {
    private String apiKey;
    private String crawlerEmail;
    private String crawlerPassword;
    private String jsonPath;
    private String fileSavePath;
    private String gptBackupSavePath;
    private String crawlerDomain;
    private String globalDomain;
    private String googleClientId;
    private String googleClientSecret;
    private String googleRedirectUri;
    private String mongodbUri;
    private String backendUsername;
    private String backendPassword;

    public String getBackendUsername() {
        return backendUsername;
    }

    public void setBackendUsername(String backendUsername) {
        this.backendUsername = backendUsername;
    }

    public String getBackendPassword() {
        return backendPassword;
    }

    public void setBackendPassword(String backendPassword) {
        this.backendPassword = backendPassword;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getCrawlerEmail() {
        return crawlerEmail;
    }

    public void setCrawlerEmail(String crawlerEmail) {
        this.crawlerEmail = crawlerEmail;
    }

    public String getCrawlerPassword() {
        return crawlerPassword;
    }

    public void setCrawlerPassword(String crawlerPassword) {
        this.crawlerPassword = crawlerPassword;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public String getFileSavePath() {
        return fileSavePath;
    }

    public void setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath;
    }

    public String getGptBackupSavePath() {
        return gptBackupSavePath;
    }

    public void setGptBackupSavePath(String gptBackupSavePath) {
        this.gptBackupSavePath = gptBackupSavePath;
    }

    public String getCrawlerDomain() {
        return crawlerDomain;
    }

    public void setCrawlerDomain(String crawlerDomain) {
        this.crawlerDomain = crawlerDomain;
    }

    public String getGlobalDomain() {
        return globalDomain;
    }

    public void setGlobalDomain(String globalDomain) {
        this.globalDomain = globalDomain;
    }

    public String getGoogleClientId() {
        return googleClientId;
    }

    public void setGoogleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
    }

    public String getGoogleClientSecret() {
        return googleClientSecret;
    }

    public void setGoogleClientSecret(String googleClientSecret) {
        this.googleClientSecret = googleClientSecret;
    }

    public String getGoogleRedirectUri() {
        return googleRedirectUri;
    }

    public void setGoogleRedirectUri(String googleRedirectUri) {
        this.googleRedirectUri = googleRedirectUri;
    }

    public String getMongodbUri() {
        return mongodbUri;
    }

    public void setMongodbUri(String mongodbUri) {
        this.mongodbUri = mongodbUri;
    }
}

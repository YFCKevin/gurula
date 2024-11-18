package com.yfckevin.cms.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vision.v1.*;
import com.yfckevin.cms.ConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class OCRServiceImpl implements OCRService{
    private final ImageAnnotatorClient visionClient;
    private final ConfigProperties configProperties;

    public OCRServiceImpl(ConfigProperties configProperties) throws IOException {
        this.configProperties = configProperties;
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(() -> ServiceAccountCredentials.fromStream(new FileInputStream(this.configProperties.getVisionAIJsonPath() + "vision-secret-key.json")))
                .build();
        visionClient = ImageAnnotatorClient.create(settings);
    }

    @Override
    public String extractText(List<AnnotateImageRequest> requests) {
        AnnotateImageResponse response = visionClient.batchAnnotateImages(requests).getResponsesList().get(0);

        if (response.hasError()) {
            log.error("Google Vision發生錯誤：" + response.getError().getMessage());
            throw new RuntimeException("error");
        }

        TextAnnotation annotations = response.getFullTextAnnotation();
        final String text = annotations.getText();

        return text;
    }
}

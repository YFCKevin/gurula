package com.yfckevin.cms.service;

import com.google.cloud.vision.v1.AnnotateImageRequest;

import java.util.List;

public interface OCRService {
    String extractText(List<AnnotateImageRequest> requests);
}

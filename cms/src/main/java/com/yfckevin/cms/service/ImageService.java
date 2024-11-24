package com.yfckevin.cms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yfckevin.common.dto.inkCloud.WorkFlowDTO;

import java.io.IOException;

public interface ImageService {
    void generateImage(WorkFlowDTO workFlowDTO) throws IOException;
}

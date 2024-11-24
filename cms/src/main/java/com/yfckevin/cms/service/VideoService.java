package com.yfckevin.cms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yfckevin.common.dto.inkCloud.WorkFlowDTO;

public interface VideoService {
    void generateVideo(WorkFlowDTO workFlowDTO) throws JsonProcessingException;
}

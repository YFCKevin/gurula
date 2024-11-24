package com.yfckevin.cms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yfckevin.common.dto.inkCloud.WorkFlowDTO;

public interface AudioService {
    void textToSpeech(WorkFlowDTO workFlowDTO) throws JsonProcessingException;
}

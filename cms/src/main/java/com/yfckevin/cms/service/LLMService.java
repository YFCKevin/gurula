package com.yfckevin.cms.service;

import com.yfckevin.common.dto.inkCloud.NarrationMsgDTO;
import com.yfckevin.common.exception.ResultStatus;

public interface LLMService {
    ResultStatus<String> constructBookEntity(String rawText);
    void constructNarration(NarrationMsgDTO narrationMsgDTO); //製作旁白
}

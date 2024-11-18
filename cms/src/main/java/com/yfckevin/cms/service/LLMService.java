package com.yfckevin.cms.service;

import com.yfckevin.common.exception.ResultStatus;

public interface LLMService {
    ResultStatus<String> constructBookEntity(String rawText);
}

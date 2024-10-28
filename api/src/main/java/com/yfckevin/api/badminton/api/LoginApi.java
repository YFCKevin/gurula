package com.yfckevin.api.badminton.api;

import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Body;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.badminton.dto.LoginDTO;
import com.yfckevin.common.exception.ResultStatus;
import org.springframework.stereotype.Component;

@Component
@BaseRequest(baseURL = "${backendDomain}")
public interface LoginApi {

    @Post(url = "/loginCheck", contentType = "application/json")
    ForestResponse<ResultStatus<?>> loginCheck(@Body LoginDTO dto);
}

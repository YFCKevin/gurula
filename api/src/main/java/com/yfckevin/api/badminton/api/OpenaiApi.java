package com.yfckevin.api.badminton.api;

import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Body;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.badminton.dto.PostResponseDTO;
import com.yfckevin.api.badminton.dto.RequestPostDTO;
import com.yfckevin.common.exception.ResultStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@BaseRequest(baseURL = "${backendDomain}")
public interface OpenaiApi {
    @Post(
            url = "/convertToPosts",
            async = true,
            contentType = "application/json"
    )
    ForestResponse<ResultStatus<List<PostResponseDTO>>> convertToPosts(@Body List<RequestPostDTO> dtoList);
}

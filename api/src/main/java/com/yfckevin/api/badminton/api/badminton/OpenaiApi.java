package com.yfckevin.api.badminton.api.badminton;

import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Body;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.badminton.dto.badminton.PostResponseDTO;
import com.yfckevin.api.badminton.dto.badminton.RequestPostDTO;
import com.yfckevin.common.exception.ResultStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@BaseRequest(baseURL = "${badmintonDomain}")
public interface OpenaiApi {
    @Post(
            url = "/convertToPosts",
            async = true,
            contentType = "application/json"
    )
    ForestResponse<ResultStatus<List<PostResponseDTO>>> convertToPosts(@Body List<RequestPostDTO> dtoList);
}

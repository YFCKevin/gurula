package com.yfckevin.api.badminton.api;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.badminton.dto.PostRequestDTO;
import com.yfckevin.api.badminton.dto.PostResponseDTO;
import com.yfckevin.api.badminton.dto.SearchDTO;
import com.yfckevin.common.exception.ResultStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@BaseRequest(baseURL = "${backendDomain}")
public interface PostApi {
    @Get(url = "/getTodayNewPosts/{startOfToday}/{endOfToday}")
    ForestResponse<ResultStatus<List<PostResponseDTO>>> todayNewPosts(@Var("startOfToday") String startOfToday, @Var("endOfToday") String endOfToday);

    @Post(url = "/searchPosts", contentType = "application/json")
    ForestResponse<List<PostResponseDTO>> searchPosts(@Body SearchDTO searchDTO);

    @Get(url = "/getPostInfo/{id}")
    ForestResponse<ResultStatus<PostResponseDTO>> onePostInfo(@Var("id") String id);

    @Get(url = "/chooseDayOfWeek/{day}")
    ForestResponse<ResultStatus<Map<String, Object>>> chooseDayOfWeek(@Var("day") String day);

    @Post(url = "/savePost", contentType = "application/json")
    ForestResponse<ResultStatus<?>> savePost(@Body PostRequestDTO dto);

    @Post(url = "/savePost", contentType = "application/json")
    ForestResponse<ResultStatus<?>> editPost(@Body PostRequestDTO dto);

    @Get(url = "/deletePost/{id}")
    ForestResponse<ResultStatus<?>> deletePost(@Var("id") String id);

    @Post(url = "/deleteAllPosts", contentType = "application/json")
    ForestResponse<ResultStatus<?>> deleteSelectedPost(@Body List<String> postIdList);

    @Get(url = "/findSamePosts")
    ForestResponse<ResultStatus<List<PostResponseDTO>>> findSamePosts();

    @Post(url = "/findPostByIdIn", contentType = "application/json")
    ForestResponse<ResultStatus<List<PostResponseDTO>>> findPostByIdIn(@Body List<String> postIds);

    @Post(url = "/saveAllMatedPosts", contentType = "application/json")
    ForestResponse<ResultStatus<?>> saveAllMatedPosts(@Body Set<String> postIds);
}

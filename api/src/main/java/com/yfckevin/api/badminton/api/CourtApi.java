package com.yfckevin.api.badminton.api;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.badminton.dto.CourtResponseDTO;
import com.yfckevin.api.badminton.dto.NearByRequestDTO;
import com.yfckevin.api.badminton.dto.SearchDTO;
import com.yfckevin.common.exception.ResultStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@BaseRequest(baseURL = "${backendDomain}")
public interface CourtApi {

    @Get(url = "/getCourtInfoByPostId/{postId}")
    ForestResponse<ResultStatus<CourtResponseDTO>> getCourtInfoByPostId(@Var("postId") String postId);

    @Get(url = "/getCourtInfoById/{id}")
    ForestResponse<ResultStatus<CourtResponseDTO>> getCourtInfoById(@Var("id") String id);

    @Post(url = "/getNearbyCourts", contentType = "application/json")
    ForestResponse<ResultStatus<List<CourtResponseDTO>>> getNearbyCourts(@Body NearByRequestDTO dto);

    @Get(url = "/getCourtAvailabilityInfo/{id}")
    ForestResponse<ResultStatus<Map<String, Object>>> getCourtAvailabilityInfo(@Var("id") String id);

    @Get(url = "/getAllCourt")
    ForestResponse<ResultStatus<List<CourtResponseDTO>>> getAllCourt();

    @Post(url = "/saveAllCourts", contentType = "application/json")
    ForestResponse<ResultStatus<?>> saveAllCourts(@Body List<CourtResponseDTO> courtList);

    @Post(url = "/saveCourt", contentType = "application/json")
    ForestResponse<ResultStatus<?>> saveCourt(@Body CourtResponseDTO court);

    @Post(url = "/searchCourt", contentType = "application/json")
    ForestResponse<ResultStatus<List<CourtResponseDTO>>> searchCourt(@Body SearchDTO searchDTO);
}

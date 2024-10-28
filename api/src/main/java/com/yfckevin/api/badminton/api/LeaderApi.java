package com.yfckevin.api.badminton.api;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.badminton.dto.LeaderRequestDTO;
import com.yfckevin.api.badminton.dto.LeaderResponseDTO;
import com.yfckevin.api.badminton.dto.LeaderUserIdListDTO;
import com.yfckevin.api.badminton.dto.SearchDTO;
import com.yfckevin.common.exception.ResultStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@BaseRequest(baseURL = "${backendDomain}")
public interface LeaderApi {

    @Post(url = "/allLeaderByUserIds", contentType = "application/json")
    ForestResponse<ResultStatus<List<LeaderResponseDTO>>> getAllLeaderByUserIds(@Body LeaderUserIdListDTO dto);

    @Get(url = "/getLeaderInfoByUserId/{userId}")
    ForestResponse<ResultStatus<LeaderResponseDTO>> oneLeaderInfo(@Var("userId") String userId);

    @Get(url = "/getLeaderInfoById/{id}")
    ForestResponse<ResultStatus<LeaderResponseDTO>> getLeaderInfoById(@Var("id") String id);

    @Get(url = "/findAllLeader")
    ForestResponse<ResultStatus<List<LeaderResponseDTO>>> findAllLeader();

    @Post(url = "/saveLeader", contentType = "application/json")
    ForestResponse<ResultStatus<?>> saveLeader(@Body LeaderRequestDTO dto);

    @Post(url = "/saveLeader", contentType = "application/json")
    ForestResponse<ResultStatus<?>> editLeader(@Body LeaderRequestDTO dto);

    @Get(url = "/deleteLeader/{id}")
    ForestResponse<ResultStatus<?>> deleteLeader(@Var("id") String id);

    @Post(url = "/leaderSearch", contentType = "application/json")
    ForestResponse<ResultStatus<List<LeaderResponseDTO>>> leaderSearch(@Body SearchDTO searchDTO);
}

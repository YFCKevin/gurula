package com.yfckevin.lineservice.api;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.lineservice.dto.LineUserProfileResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@BaseRequest(baseURL = "https://api.line.me/v2/bot")
public interface LineApi {
    @Post(
            url = "/push",
            contentType = "application/json"
    )
    ForestResponse<Void> pushOne(@Header("Authorization") String accessToken, @Body Map<String, Object> dataList);

    @Post(
            url = "/message/multicast",
            contentType = "application/json"
    )
    ForestResponse<Void> pushMulti(@Header("Authorization") String accessToken, @Body Map<String, Object> dataList);

    @Get(url = "/profile/{userId}")
    ForestResponse<LineUserProfileResponseDTO> getUserProfile(@Header("Authorization") String channelAccessToken, @Var("userId") String userId);
}

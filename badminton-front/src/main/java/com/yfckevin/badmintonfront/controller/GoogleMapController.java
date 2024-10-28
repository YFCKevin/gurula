package com.yfckevin.badmintonfront.controller;

import com.dtflys.forest.http.ForestResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfckevin.api.badminton.api.CourtApi;
import com.yfckevin.api.badminton.dto.NearByRequestDTO;
import com.yfckevin.badmintonfront.dto.PostDTO;
import com.yfckevin.api.badminton.dto.CourtResponseDTO;
import com.yfckevin.api.badminton.dto.LeaderResponseDTO;
import com.yfckevin.api.badminton.dto.PostResponseDTO;
import com.yfckevin.common.exception.ResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class GoogleMapController {

    private final CourtApi courtApi;
    private final DateTimeFormatter ddf;
    Logger logger = LoggerFactory.getLogger(GoogleMapController.class);
    private final ObjectMapper objectMapper;

    public GoogleMapController(CourtApi courtApi, DateTimeFormatter ddf, ObjectMapper objectMapper) {
        this.courtApi = courtApi;
        this.ddf = ddf;
        this.objectMapper = objectMapper;
    }

    /**
     * 導頁到地圖介面
     * @return
     */
    @GetMapping("/map")
    public String mapPage() {
        return "map";
    }


    /**
     * 取得指定方圓距離的球場
     *
     * @param dto
     * @return
     */
    @PostMapping("/getNearbyCourts")
    public ResponseEntity<?> getNearbyCourts(@RequestBody NearByRequestDTO dto) {
        logger.info("[getNearbyCourts]");
        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<List<CourtResponseDTO>>> nearbyCourts = courtApi.getNearbyCourts(dto);
        if ("C000".equals(nearbyCourts.getResult().getCode())){
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(nearbyCourts.getResult().getData());
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 用球場編號查詢貼文
     * @param id
     * @return
     */
    @GetMapping("/getCourtAvailabilityInfo/{id}")
    public ResponseEntity<?> getCourtAvailabilityInfo (@PathVariable String id){
        logger.info("[getCourtAvailabilityInfo]");
        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<Map<String, Object>>> courtAvailabilityInfo = courtApi.getCourtAvailabilityInfo(id);
        if ("C000".equals(courtAvailabilityInfo.getResult().getCode())) {
            final Map<String, Object> data = courtAvailabilityInfo.getResult().getData();
            List<LinkedHashMap<String, Object>> leaderMapList = (List<LinkedHashMap<String, Object>>) data.get("leaderList");
            List<LinkedHashMap<String, Object>> postMapList = (List<LinkedHashMap<String, Object>>) data.get("postList");
            List<LeaderResponseDTO> leaderList = leaderMapList.stream()
                    .map(item -> objectMapper.convertValue(item, LeaderResponseDTO.class))
                    .collect(Collectors.toList());
            List<PostResponseDTO> postList = postMapList.stream()
                    .map(item -> objectMapper.convertValue(item, PostResponseDTO.class))
                    .collect(Collectors.toList());

            final Map<String, LeaderResponseDTO> leaderMap = leaderList
                    .stream()
                    .collect(Collectors.toMap(LeaderResponseDTO::getUserId, Function.identity()));

            List<PostDTO> postDTOList = postList.stream()
                    .map(post -> {
                        try {
                            return constructPostDTO(leaderMap, post);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .sorted(Comparator.comparing(PostDTO::getStartTime))
                    .collect(Collectors.toList());

            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(postDTOList);
        }
        return ResponseEntity.ok(resultStatus);
    }


    private PostDTO constructPostDTO(Map<String, LeaderResponseDTO> leaderMap, PostResponseDTO post) throws ParseException {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(post.getId());
        postDTO.setCreationDate(post.getCreationDate());
        postDTO.setBrand(post.getBrand());
        postDTO.setDuration(post.getDuration());
        postDTO.setName(post.getName());
        postDTO.setContact(post.getContact());
        postDTO.setFee(post.getFee());
        postDTO.setAirConditioner(post.getAirConditioner().getLabel());
        postDTO.setType(post.getType());
        postDTO.setEndTime(post.getEndTime());
        postDTO.setStartTime(post.getStartTime());
        postDTO.setLevel(post.getLevel());
        postDTO.setParkInfo(post.getParkInfo());
        postDTO.setPlace(post.getPlace());
        postDTO.setUserId(post.getUserId());
        postDTO.setLabelCourt(String.valueOf(post.isLabelCourt()));
        LeaderResponseDTO leader = leaderMap.get(post.getUserId());
        if (leader != null) {
            postDTO.setLink(leader.getLink());
            postDTO.setShortLink("https://www.facebook.com/" + leader.getUserId());
        }


        if (post.getStartTime() != null && post.getEndTime() != null) {
            LocalDateTime startDateTime = LocalDateTime.parse(post.getStartTime(), ddf);
            LocalDateTime endDateTime = LocalDateTime.parse(post.getEndTime(), ddf);

            // 取得星期
            DayOfWeek dayOfWeek = startDateTime.getDayOfWeek();
            // 格式化星期
            String dayOfWeekFormatted = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.TAIWAN);
            postDTO.setDayOfWeek(dayOfWeekFormatted);
            final String formattedStartDate = startDateTime.format(DateTimeFormatter.ofPattern("MM/dd"));
            final String formattedStartTime = startDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            final String formattedEndTime = endDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));

            postDTO.setTime(formattedStartDate + "(" + dayOfWeekFormatted + ") " + formattedStartTime + " - " + formattedEndTime + " (" + formatDuration(post.getDuration()) + "h)");
        }

        return postDTO;
    }

    public String formatDuration(double duration) {
        double hours = duration / 60;
        if (hours == (int) hours) {
            // 如果是整數
            return String.format("%.0f", hours);
        } else {
            String hoursStr = String.format("%.2f", hours);
            if (hoursStr.endsWith("0")) {
                // 去掉末尾的零
                hoursStr = hoursStr.substring(0, hoursStr.length() - 1);
            }
            return hoursStr;
        }
    }
}

package com.yfckevin.badmintonfront.controller;

import com.dtflys.forest.http.ForestResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfckevin.api.badminton.api.LeaderApi;
import com.yfckevin.api.badminton.api.PostApi;
import com.yfckevin.badmintonfront.dto.PostDTO;
import com.yfckevin.api.badminton.dto.SearchDTO;
import com.yfckevin.api.badminton.dto.LeaderResponseDTO;
import com.yfckevin.api.badminton.dto.LeaderUserIdListDTO;
import com.yfckevin.api.badminton.dto.PostResponseDTO;
import com.yfckevin.common.exception.ResultStatus;
import com.yfckevin.common.utils.MemberContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class PostController {
    Logger logger = LoggerFactory.getLogger(PostController.class);
    private final DateTimeFormatter ddf;
    private final PostApi postApi;
    private final LeaderApi leaderApi;
    private final ObjectMapper objectMapper;

    public PostController(DateTimeFormatter ddf, PostApi postApi, LeaderApi leaderApi, ObjectMapper objectMapper) {
        this.ddf = ddf;
        this.postApi = postApi;
        this.leaderApi = leaderApi;
        this.objectMapper = objectMapper;
    }

    /**
     * 前往貼文清單頁
     *
     * @return
     */
    @GetMapping("/posts")
    public String posts(Model model) {
        logger.info("[posts]");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime threeDaysLater = now.plusWeeks(2);
        LocalDateTime endOfThreeDaysLater = threeDaysLater.withHour(23).withMinute(59).withSecond(59).withNano(0);
        String startOfTodayFormatted = startOfToday.format(ddf);
        String endOfThreeDaysLaterFormatted = endOfThreeDaysLater.format(ddf);

        List<PostResponseDTO> postList = new ArrayList<>();

        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setKeyword("");
        searchDTO.setStartDate(startOfTodayFormatted);
        searchDTO.setEndDate(endOfThreeDaysLaterFormatted);
        final ForestResponse<List<PostResponseDTO>> searchPosts = postApi.searchPosts(searchDTO);
        if (searchPosts.isSuccess()) {
            postList = searchPosts.getResult().stream().limit(10).collect(Collectors.toList());
        }

        final Set<String> userIdList = postList.stream().map(PostResponseDTO::getUserId).collect(Collectors.toSet());
        Map<String, LeaderResponseDTO> leaderMap = new HashMap<>();
        LeaderUserIdListDTO dto = new LeaderUserIdListDTO();
        dto.setUserIdList(userIdList);
        final ForestResponse<ResultStatus<List<LeaderResponseDTO>>> allLeader = leaderApi.getAllLeaderByUserIds(dto);
        if ("C000".equals(allLeader.getResult().getCode())) {
            leaderMap = allLeader.getResult().getData()
                    .stream()
                    .collect(Collectors.toMap(LeaderResponseDTO::getUserId, Function.identity()));
        }

        Map<String, LeaderResponseDTO> finalLeaderMap = leaderMap;
        final List<PostDTO> postDTOList = postList
                .stream()
                .map(post -> {
                    try {
                        return constructPostDTO(finalLeaderMap, post);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .sorted(Comparator.comparing(PostDTO::getStartTime).reversed())
                .collect(Collectors.toList());

        model.addAttribute("posts", postDTOList);
//
//        Credential credential = (Credential) session.getAttribute("credential");
//        if (credential != null) {
//            ClientParametersAuthentication clientAuth = (ClientParametersAuthentication) credential.getClientAuthentication();
//
//            model.addAttribute("clientId", clientAuth.getClientId());
//
//        }


        return "post";
    }


    /**
     * 用團主名、地點、用球、停車資訊、起迄時間做模糊查詢
     *
     * @param searchDTO
     * @return
     */
    @PostMapping("/searchPosts")
    public ResponseEntity<?> searchPosts(@RequestBody SearchDTO searchDTO) throws ParseException {
        logger.info("[searchPosts]");
        ResultStatus resultStatus = new ResultStatus();
        List<PostResponseDTO> postList = new ArrayList<>();

        final ForestResponse<List<PostResponseDTO>> searchPosts = postApi.searchPosts(searchDTO);
        if (searchPosts.isSuccess()) {
            postList = searchPosts.getResult().stream().limit(10).collect(Collectors.toList());
        }

        final Set<String> userIdList = postList.stream().map(PostResponseDTO::getUserId).collect(Collectors.toSet());
        Map<String, LeaderResponseDTO> leaderMap = new HashMap<>();
        LeaderUserIdListDTO dto = new LeaderUserIdListDTO();
        dto.setUserIdList(userIdList);
        final ForestResponse<ResultStatus<List<LeaderResponseDTO>>> allLeader = leaderApi.getAllLeaderByUserIds(dto);
        if ("C000".equals(allLeader.getResult().getCode())) {
            leaderMap = allLeader.getResult().getData()
                    .stream()
                    .collect(Collectors.toMap(LeaderResponseDTO::getUserId, Function.identity()));
        }

        Map<String, LeaderResponseDTO> finalLeaderMap = leaderMap;
        final List<PostDTO> postDTOList = postList
                .stream()
                .map(post -> {
                    try {
                        return constructPostDTO(finalLeaderMap, post);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .sorted(Comparator.comparing(PostDTO::getStartTime).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(postDTOList);
    }


    @GetMapping("/chooseDayOfWeek/{day}")
    public ResponseEntity<?> chooseDayOfWeek (@PathVariable String day){
        logger.info("[chooseDayOfWeek]");
        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<Map<String, Object>>> chooseDayOfWeek = postApi.chooseDayOfWeek(day);
        if ("C000".equals(chooseDayOfWeek.getResult().getCode())) {
            final Map<String, Object> data = chooseDayOfWeek.getResult().getData();
            List<LinkedHashMap<String, Object>> leaderMapList = (List<LinkedHashMap<String, Object>>) data.get("leaderList");
            List<LinkedHashMap<String, Object>> postMapList = (List<LinkedHashMap<String, Object>>) data.get("postList");
            List<LeaderResponseDTO> leaderList = leaderMapList.stream()
                    .map(item -> objectMapper.convertValue(item, LeaderResponseDTO.class))
                    .collect(Collectors.toList());
            List<PostResponseDTO> postList = postMapList.stream()
                    .map(item -> objectMapper.convertValue(item, PostResponseDTO.class))
                    .collect(Collectors.toList());
            List<PostDTO> postDTOList = new ArrayList<>();
            final Map<String, LeaderResponseDTO> leaderMap = leaderList.stream()
                    .collect(Collectors.toMap(LeaderResponseDTO::getUserId, Function.identity()));
            postDTOList = postList.stream()
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

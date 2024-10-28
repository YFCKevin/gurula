package com.yfckevin.badmintonfront.controller;

import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.badminton.api.LeaderApi;
import com.yfckevin.api.badminton.api.PostApi;
import com.yfckevin.badmintonfront.dto.PostDTO;
import com.yfckevin.api.badminton.dto.SearchDTO;
import com.yfckevin.api.badminton.dto.LeaderResponseDTO;
import com.yfckevin.api.badminton.dto.LeaderUserIdListDTO;
import com.yfckevin.api.badminton.dto.PostResponseDTO;
import com.yfckevin.common.exception.ResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
public class IndexController {
    private final DateTimeFormatter ddf;
    Logger logger = LoggerFactory.getLogger(IndexController.class);
    private final PostApi postApi;
    private final LeaderApi leaderApi;

    public IndexController(DateTimeFormatter ddf, PostApi postApi, LeaderApi leaderApi) {
        this.ddf = ddf;
        this.postApi = postApi;
        this.leaderApi = leaderApi;
    }

    @GetMapping("/index")
    public String indexPage (HttpSession session, Model model) throws ParseException {

        logger.info("[indexPage]");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfTodayLater = now.withHour(23).withMinute(59).withSecond(59).withNano(0);
        String startOfTodayNewPosts = startOfToday.format(ddf);
        String endOfTodayNewPosts = endOfTodayLater.format(ddf);

        final ForestResponse<ResultStatus<List<PostResponseDTO>>> todayNewPosts = postApi.todayNewPosts(startOfTodayNewPosts, endOfTodayNewPosts);
        List<PostResponseDTO> todayPosts = new ArrayList<>();
        if ("C000".equals(todayNewPosts.getResult().getCode())) {
            todayPosts = todayNewPosts.getResult().getData();
        }
        if (todayPosts.size() == 0) {   //新貼文尚未抓取，先放今日零打資訊
            SearchDTO dto = new SearchDTO();
            dto.setKeyword("");
            dto.setStartDate(startOfTodayNewPosts);
            dto.setEndDate(endOfTodayNewPosts);
            ForestResponse<List<PostResponseDTO>> searchPosts = postApi.searchPosts(dto);
            if (!searchPosts.isSuccess()) {
                return "/error/50x";
            } else {
                todayPosts = searchPosts.getResult().stream().limit(10).collect(Collectors.toList());
            }
        }

        final Set<String> userIdList = todayPosts.stream().map(PostResponseDTO::getUserId).collect(Collectors.toSet());
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
        final List<PostDTO> todayNewPostList = todayPosts
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

        model.addAttribute("todayNewPostList", todayNewPostList);
        return "index";
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

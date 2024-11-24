package com.yfckevin.badmintonfront.controller;

import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.api.badminton.CourtApi;
import com.yfckevin.api.dto.badminton.PostResponseDTO;
import com.yfckevin.badmintonfront.dto.LeaderDTO;
import com.yfckevin.badmintonfront.dto.PostDTO;
import com.yfckevin.api.api.badminton.LeaderApi;
import com.yfckevin.api.api.badminton.PostApi;
import com.yfckevin.api.dto.badminton.CourtResponseDTO;
import com.yfckevin.api.dto.badminton.LeaderResponseDTO;
import com.yfckevin.common.exception.ResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Controller
public class CardController {
    private final PostApi postApi;
    private final LeaderApi leaderApi;
    private final CourtApi courtApi;
    private final DateTimeFormatter ddf;
    Logger logger = LoggerFactory.getLogger(CardController.class);

    public CardController(PostApi postApi, LeaderApi leaderApi, CourtApi courtApi, DateTimeFormatter ddf) {
        this.postApi = postApi;
        this.leaderApi = leaderApi;
        this.courtApi = courtApi;
        this.ddf = ddf;
    }


    @GetMapping("/card/{id}")
    public String card (@PathVariable String id, Model model){
        logger.info("[card]");

        final ForestResponse<ResultStatus<PostResponseDTO>> onePostInfo = postApi.onePostInfo(id);
        if (!onePostInfo.isSuccess()) {
            return "error/50x";
        } else {
            if ("C000".equals(onePostInfo.getResult().getCode())) {
                final PostResponseDTO post = onePostInfo.getResult().getData();

                LeaderResponseDTO leader = null;
                final ForestResponse<ResultStatus<LeaderResponseDTO>> oneLeaderInfo = leaderApi.oneLeaderInfo(post.getUserId());
                if (!oneLeaderInfo.isSuccess()) {
                    return "error/50x";
                } else {
                    if ("C000".equals(oneLeaderInfo.getResult().getCode())) {
                        leader = oneLeaderInfo.getResult().getData();
                        model.addAttribute("leaderDTO", constructLeaderDTO(leader));
                    }
                }

                final PostDTO postDTO = constructDTO(leader, post);
                model.addAttribute("postDTO", postDTO);

                final ForestResponse<ResultStatus<CourtResponseDTO>> oneCourtInfo = courtApi.getCourtInfoByPostId(post.getId());
                if (!oneCourtInfo.isSuccess()) {
                    return "error/50x";
                } else {
                    if ("C000".equals(oneCourtInfo.getResult().getCode())) {
                        final CourtResponseDTO court = oneCourtInfo.getResult().getData();
                        model.addAttribute("court", court);
                    }
                }
            }
        }
        return "card";
    }


    private PostDTO constructDTO (LeaderResponseDTO leader, PostResponseDTO post){
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


    private static LeaderDTO constructLeaderDTO(LeaderResponseDTO leader) {
        LeaderDTO dto = new LeaderDTO();
        dto.setId(leader.getId());
        dto.setName(leader.getName());
        dto.setUserId(leader.getUserId());
        final String groupId = leader.getGroupId();
        dto.setGroupId(groupId);
        dto.setLink(leader.getLink());
        dto.setCreationDate(leader.getCreationDate());
        dto.setModificationDate(leader.getModificationDate());
        dto.setDeletionDate(leader.getDeletionDate());
        if ("392553431115145".equals(groupId)) {
            dto.setGroupName("大台北羽球同好交流版");
        } else if ("1882953728686436".equals(groupId)) {
            dto.setGroupName("新北市羽球臨打揪團");
        } else if ("480573685305042".equals(groupId)) {
            dto.setGroupName("台北基隆北北基羽球同好交流區");
        }
        return dto;
    }
}

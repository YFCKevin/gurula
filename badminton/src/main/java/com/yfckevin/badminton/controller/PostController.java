package com.yfckevin.badminton.controller;

import com.yfckevin.badminton.dto.PostDTO;
import com.yfckevin.badminton.dto.SearchDTO;
import com.yfckevin.badminton.entity.Leader;
import com.yfckevin.badminton.entity.Post;
import com.yfckevin.badminton.service.LeaderService;
import com.yfckevin.badminton.service.PostService;
import com.yfckevin.common.exception.ResultStatus;
import com.yfckevin.common.utils.MemberContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class PostController {

    private final PostService postService;
    private final LeaderService leaderService;
    private final DateTimeFormatter ddf;
    Logger logger = LoggerFactory.getLogger(PostController.class);

    public PostController(PostService postService, LeaderService leaderService, DateTimeFormatter ddf) {
        this.postService = postService;
        this.leaderService = leaderService;
        this.ddf = ddf;
    }


    /**
     * 取得每日新貼文
     * @param session
     * @return
     */
    @GetMapping("/getTodayNewPosts/{startOfToday}/{endOfToday}")
    public ResponseEntity<?> getTodayNewPosts (@PathVariable String startOfToday, @PathVariable String endOfToday, HttpSession session){

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[getTodayNewPosts]");
        }

        ResultStatus resultStatus = new ResultStatus();

        // 每日新貼文
        final List<Post> todayPosts = postService.findTodayNewPosts(startOfToday, endOfToday);
        final Set<String> userIdList = todayPosts.stream().map(Post::getUserId).collect(Collectors.toSet());
        final Map<String, Leader> leaderMap = leaderService.findAllByUserIdIn(userIdList)
                .stream()
                .collect(Collectors.toMap(Leader::getUserId, Function.identity()));

        final List<PostDTO> todayNewPostList = todayPosts
                .stream()
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
        resultStatus.setData(todayNewPostList);

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 用團主名、地點、用球、停車資訊、起迄時間做模糊查詢
     *
     * @param dto
     * @return
     */
    @PostMapping("/searchPosts")
    public ResponseEntity<?> searchPosts(@RequestBody SearchDTO dto) throws ParseException {
        logger.info("[searchPosts]");
        List<Post> postList = postService.findPostByConditions(dto.getKeyword().trim(), dto.getStartDate(), dto.getEndDate());
        return ResponseEntity.ok(postList);
    }



    @GetMapping("/chooseDayOfWeek/{day}")
    public ResponseEntity<?> chooseDayOfWeek (@PathVariable String day){
        logger.info("[chooseDayOfWeek]");

        ResultStatus resultStatus = new ResultStatus();

        List<Post> postList = postService.findPostsByDaySorted(day, getNextDate(day).toString());

        final Set<String> userIdList = postList.stream().map(Post::getUserId).collect(Collectors.toSet());

        final List<Leader> leaderList = leaderService.findAllByUserIdIn(userIdList);

        Map<String, Object> result = new HashMap<>();
        result.put("leaderList", leaderList);
        result.put("postList", postList);

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(result);

        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/getPostInfo/{id}")
    public ResponseEntity<?> getPostInfo (@PathVariable String id){
        logger.info("[getPostInfo]");
        ResultStatus resultStatus = new ResultStatus();
        final Optional<Post> opt = postService.findById(id);
        if (opt.isEmpty()) {
            resultStatus.setCode("C003");
            resultStatus.setMessage("查無貼文");
        } else {
            final Post post = opt.get();
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(post);
        }
        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/findSamePosts")
    public ResponseEntity<?> findSamePosts (){
        logger.info("[findSamePosts]");
        ResultStatus resultStatus = new ResultStatus();
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(postService.findSamePosts());
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/findPostByIdIn")
    public ResponseEntity<?> findPostByIdIn (@RequestBody List<String> postIds){
        logger.info("[findPostByIdIn]");
        ResultStatus resultStatus = new ResultStatus();
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(postService.findByIdIn(postIds));
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/saveAllMatedPosts")
    public ResponseEntity<?> saveAllMatedPosts (@RequestBody Set<String> postIdSet){
        logger.info("[saveAllPosts]");
        ResultStatus resultStatus = new ResultStatus();
        List<String> postIds = new ArrayList<>(postIdSet);
        final List<Post> postList = postService.findByIdIn(postIds).stream()
                .peek(post -> post.setLabelCourt(true)).collect(Collectors.toList());
        postService.saveAll(postList);
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        return ResponseEntity.ok(resultStatus);
    }


    public static LocalDate getNextDate(String dayOfWeekStr) {
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(dayOfWeekStr.toUpperCase());
        LocalDate now = LocalDate.now();

        // 找到當前是星期幾
        DayOfWeek currentDayOfWeek = now.getDayOfWeek();

        // 計算相差天數
        int daysToAdd = dayOfWeek.getValue() - currentDayOfWeek.getValue();
        if (daysToAdd < 0) {
            daysToAdd += 7;
        }

        return now.plusDays(daysToAdd);
    }


    private PostDTO constructPostDTO(Map<String, Leader> leaderMap, Post post) throws ParseException {
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
        Leader leader = leaderMap.get(post.getUserId());
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

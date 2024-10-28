package com.yfckevin.badminton.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.yfckevin.badminton.ConfigProperties;
import com.yfckevin.badminton.dto.*;
import com.yfckevin.badminton.entity.*;
import com.yfckevin.badminton.enums.AirConditionerType;
import com.yfckevin.badminton.service.*;
import com.yfckevin.badminton.utils.ConfigurationUtil;
import com.yfckevin.common.exception.ResultStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class BackendManagementController {
    Logger logger = LoggerFactory.getLogger(BackendManagementController.class);
    private final ConfigProperties configProperties;
    private final LeaderService leaderService;
    private final PostService postService;
    private final OpenAiService openAiService;
    private final SimpleDateFormat sdf;
    private final DateTimeFormatter ddf;

    public BackendManagementController(ConfigProperties configProperties, LeaderService leaderService, PostService postService, OpenAiService openAiService, @Qualifier("sdf") SimpleDateFormat sdf, DateTimeFormatter ddf) {
        this.configProperties = configProperties;
        this.leaderService = leaderService;
        this.postService = postService;
        this.openAiService = openAiService;
        this.sdf = sdf;
        this.ddf = ddf;
    }

    /**
     * 登入驗證
     *
     * @return
     */
    @PostMapping("/loginCheck")
    public ResponseEntity<?> loginCheck(@RequestBody LoginDTO dto, HttpSession session) throws IOException {
        logger.info("[loginCheck]");
        ResultStatus resultStatus = new ResultStatus();
        ConfigurationUtil.Configuration();
        File file = new File(configProperties.getJsonPath() + "backendAccount.js");
        TypeRef<LoginDTO> typeRef = new TypeRef<>() {
        };
        final LoginDTO loginDTO = JsonPath.parse(file).read("$", typeRef);
        if (!loginDTO.getAccount().equals(dto.getAccount()) || !loginDTO.getPassword().equals(dto.getPassword())) {
            resultStatus.setCode("C001");
            resultStatus.setMessage("帳號或密碼錯誤");
        } else {
            session.setAttribute("admin", loginDTO.getAccount());
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        }
        return ResponseEntity.ok(resultStatus);
    }

    /**
     * 新增/修改團主
     *
     * @param session
     * @return
     */
    @PostMapping("/saveLeader")
    public ResponseEntity<?> saveLeader(@RequestBody LeaderDTO dto, HttpSession session) {
        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[saveLeader]");
        }
        ResultStatus resultStatus = new ResultStatus();

        String regex = "groups/(\\d+)/user/(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dto.getLink());

        if (StringUtils.isBlank(dto.getId())) { // 新增
            Leader leader = new Leader();
            leader.setName(dto.getName());
            leader.setLink(dto.getLink());
            if (matcher.find()) {
                leader.setGroupId(matcher.group(1));
                leader.setUserId(matcher.group(2));
            }
            leader.setCreationDate(sdf.format(new Date()));
            leaderService.save(leader);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        } else {    // 修改
            leaderService.findById(dto.getId())
                    .map(leader -> {
                        leader.setName(dto.getName());
                        leader.setLink(dto.getLink());
                        if (matcher.find()) {
                            leader.setGroupId(matcher.group(1));
                            leader.setUserId(matcher.group(2));
                        }
                        leader.setModificationDate(sdf.format(new Date()));
                        leaderService.save(leader);
                        resultStatus.setCode("C000");
                        resultStatus.setMessage("成功");
                        return resultStatus;
                    })
                    .orElseGet(() -> {
                        resultStatus.setCode("C002");
                        resultStatus.setMessage("查無團主");
                        return resultStatus;
                    });
        }

        return ResponseEntity.ok(resultStatus);
    }

    /**
     * 刪除團主
     *
     * @param session
     * @return
     */
    @GetMapping("/deleteLeader/{id}")
    public ResponseEntity<?> deleteLeader(@PathVariable String id, HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[deleteLeader]");
        }

        ResultStatus resultStatus = new ResultStatus();

        leaderService.findById(id)
                .map(leader -> {
                    leaderService.deleteById(id);
                    resultStatus.setCode("C000");
                    resultStatus.setMessage("成功");
                    return resultStatus;
                })
                .orElseGet(() -> {
                    resultStatus.setCode("C002");
                    resultStatus.setMessage("查無團主");
                    return resultStatus;
                });

        return ResponseEntity.ok(resultStatus);
    }

    /**
     * 查詢團主
     *
     * @param searchDTO
     * @param session
     * @return
     */
    @PostMapping("/leaderSearch")
    public ResponseEntity<?> leaderSearch(@RequestBody SearchDTO searchDTO, HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[searchLeaders]");
        }

        ResultStatus resultStatus = new ResultStatus();

        List<LeaderDTO> leaderDTOList = leaderService.findLeaderByConditions(searchDTO.getKeyword().trim(), searchDTO.getStartDate(), searchDTO.getEndDate())
                .stream()
                .map(BackendManagementController::constructLeaderDTO)
                .collect(Collectors.toList());

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(leaderDTOList);

        return ResponseEntity.ok(resultStatus);
    }

    /**
     * 新增/修改貼文
     *
     * @param session
     * @return
     */
    @PostMapping("/savePost")
    public ResponseEntity<?> savePost(@RequestBody PostDTO dto, HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[savePost]");
        }

        ResultStatus resultStatus = new ResultStatus();

        if (StringUtils.isBlank(dto.getId())) { //新增
            Post p = new Post();
            final Post post = constructPostEntity(p, dto);
            post.setCreationDate(sdf.format(new Date()));
            postService.save(post);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        } else {    //修改
            postService.findById(dto.getId())
                    .map(p -> {
                        final Post post = constructPostEntity(p, dto);
                        p.setId(dto.getId());
                        p.setModificationDate(sdf.format(new Date()));
                        postService.save(post);
                        resultStatus.setCode("C000");
                        resultStatus.setMessage("成功");
                        return resultStatus;
                    })
                    .orElseGet(() -> {
                        resultStatus.setCode("C003");
                        resultStatus.setMessage("查無貼文");
                        return resultStatus;
                    });
        }

        return ResponseEntity.ok(resultStatus);
    }

    /**
     * 刪除貼文
     *
     * @param session
     * @return
     */
    @GetMapping("/deletePost/{id}")
    public ResponseEntity<?> deletePost(@PathVariable String id, HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[deletePost]");
        }

        ResultStatus resultStatus = new ResultStatus();

        postService.findById(id)
                .map(post -> {
                    postService.deleteById(id);
                    resultStatus.setCode("C000");
                    resultStatus.setMessage("成功");
                    return resultStatus;
                })
                .orElseGet(() -> {
                    resultStatus.setCode("C003");
                    resultStatus.setMessage("查無貼文");
                    return resultStatus;
                });

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 刪除全部貼文
     *
     * @param postIdList
     * @param session
     * @return
     */
    @PostMapping("/deleteAllPosts")
    public ResponseEntity<?> deleteAllPosts(@RequestBody List<String> postIdList, HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[deleteAllPosts]");
        }

        ResultStatus resultStatus = new ResultStatus();

        postService.deleteByIdIn(postIdList);
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");

        return ResponseEntity.ok(resultStatus);
    }

    /**
     * 打OpenAI的text completion
     *
     * @param session
     * @return
     * @throws ParseException
     * @throws IOException
     */
    @PostMapping("/convertToPosts")
    public CompletableFuture<ResponseEntity<ResultStatus>> convertToPosts(@RequestBody List<RequestPostDTO> requestPostDTOList, HttpSession session) throws ParseException, IOException {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[convertToPosts]");
        }

        ResultStatus resultStatus = new ResultStatus();

        try {

            if (requestPostDTOList.size() > 0) {
                final String prompt = constructPrompt(requestPostDTOList);

                return openAiService.generatePosts(prompt)
                        .thenApply(postList -> {
                            // 結果呈現回前端
                            resultStatus.setCode("C000");
                            resultStatus.setMessage("成功");
                            resultStatus.setData(postList);
                            return ResponseEntity.ok(resultStatus);
                        })
                        .exceptionally(ex -> {
                            logger.error(ex.getMessage(), ex);
                            resultStatus.setCode("C999");
                            resultStatus.setMessage("例外發生");
                            return ResponseEntity.ok(resultStatus);
                        });

            } else {
                resultStatus.setCode("C006");
                resultStatus.setMessage("未選取檔案");
                return CompletableFuture.completedFuture(ResponseEntity.ok(resultStatus));
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultStatus.setCode("C999");
            resultStatus.setMessage("例外發生");
            return CompletableFuture.completedFuture(ResponseEntity.ok(resultStatus));
        }
    }

    private String constructPrompt(List<RequestPostDTO> postDTOList) {
        // 使用 StringBuilder 來構建文件的內容
        StringBuilder builder = new StringBuilder();

        // 將新貼文資料 append 到 builder
        for (RequestPostDTO post : postDTOList) {
            String name = post.getName();
            String postContent = post.getPostContent();
            String userId = post.getUserId();
            builder.append(name).append(" | ").append(userId).append("::: ").append(postContent).append(" /// ");
        }

        // 去掉最後的 " /// "
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 4);
        }

        logger.info("建構完成prompt");

        return builder.toString();
    }


    private List<RequestPostDTO> constructPostDTOFromDailyPostsFile(String date) throws IOException {
        ConfigurationUtil.Configuration();
        File file = new File(configProperties.getFileSavePath() + date + "-data_disposable.json");

        if (!file.exists()) {
            return Collections.emptyList();
        }
        TypeRef<List<RequestPostDTO>> typeRef = new TypeRef<>() {
        };
        return JsonPath.parse(file).read("$", typeRef);
    }


    private static LeaderDTO constructLeaderDTO(Leader leader) {
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

    private PostDTO constructPostDTO(Post post) throws ParseException {
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

        if (post.getStartTime() != null && post.getEndTime() != null) {
            LocalDateTime startDateTime = LocalDateTime.parse(post.getStartTime(), ddf);
            LocalDateTime endDateTime = LocalDateTime.parse(post.getEndTime(), ddf);

            // 取得星期
            DayOfWeek dayOfWeek = startDateTime.getDayOfWeek();
            // 格式化星期
            String dayOfWeekFormatted = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.TAIWAN);

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


    private Post constructPostEntity(Post post, PostDTO postDTO) {
        post.setBrand(postDTO.getBrand());
        post.setDuration(postDTO.getDuration());
        post.setName(postDTO.getName());
        post.setContact(postDTO.getContact());
        post.setFee(postDTO.getFee());
        switch (postDTO.getAirConditioner()) {
            case "present":
                post.setAirConditioner(AirConditionerType.present);
                break;
            case "absent":
                post.setAirConditioner(AirConditionerType.absent);
                break;
            case "no_mention":
                post.setAirConditioner(AirConditionerType.no_mention);
                break;
        }
        post.setType(postDTO.getType());
        post.setEndTime(postDTO.getEndTime());
        post.setStartTime(postDTO.getStartTime());
        post.setLevel(postDTO.getLevel());
        post.setParkInfo(postDTO.getParkInfo());
        post.setPlace(postDTO.getPlace());
        post.setUserId(postDTO.getUserId());
        post.setStartTime(postDTO.getStartTime());
        post.setEndTime(postDTO.getEndTime());

        if (StringUtils.isNotBlank(postDTO.getStartTime())) {
            post.setDayOfWeek(postDTO.getStartTime());
        }

        if (StringUtils.isNotBlank(postDTO.getEndTime()) && StringUtils.isNotBlank(postDTO.getStartTime())) {
            LocalDateTime startDateTime = LocalDateTime.parse(postDTO.getStartTime(), ddf);
            LocalDateTime endDateTime = LocalDateTime.parse(postDTO.getEndTime(), ddf);
            long duration = java.time.Duration.between(startDateTime, endDateTime).toMinutes();
            post.setDuration((int) duration);

        }

        return post;
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
}

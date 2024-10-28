package com.yfckevin.badmintonfront.controller;

import com.dtflys.forest.http.ForestResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.yfckevin.badmintonfront.ConfigProperties;
import com.yfckevin.badmintonfront.dto.PostDTO;
import com.yfckevin.api.badminton.dto.RequestPostDTO;
import com.yfckevin.api.badminton.dto.SearchDTO;
import com.yfckevin.api.badminton.api.CourtApi;
import com.yfckevin.api.badminton.api.LeaderApi;
import com.yfckevin.api.badminton.api.OpenaiApi;
import com.yfckevin.api.badminton.api.PostApi;
import com.yfckevin.badmintonfront.utils.ConfigurationUtil;
import com.yfckevin.badmintonfront.dto.*;
import com.yfckevin.api.badminton.dto.*;
import com.yfckevin.common.exception.ResultStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class ManagementController {
    Logger logger = LoggerFactory.getLogger(ManagementController.class);
    private final DateTimeFormatter ddf;
    private final SimpleDateFormat ssf;
    private final SimpleDateFormat svf;
    private final LeaderApi leaderApi;
    private final PostApi postApi;
    private final CourtApi courtApi;
    private final OpenaiApi openaiApi;
    private final ConfigProperties configProperties;
    private final ObjectMapper objectMapper;

    public ManagementController(DateTimeFormatter ddf, @Qualifier("ssf") SimpleDateFormat ssf, @Qualifier("svf") SimpleDateFormat svf, LeaderApi leaderApi, PostApi postApi, CourtApi courtApi, OpenaiApi openaiApi, ConfigProperties configProperties, ObjectMapper objectMapper) {
        this.ddf = ddf;
        this.ssf = ssf;
        this.svf = svf;
        this.leaderApi = leaderApi;
        this.postApi = postApi;
        this.courtApi = courtApi;
        this.openaiApi = openaiApi;
        this.configProperties = configProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 導團主管理頁面
     *
     * @param session
     * @param model
     * @return
     */
    @GetMapping("/forwardLeaderManagement")
    public String forwardLeaderPage(HttpSession session, Model model) {
        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[forwardLeaderManagement]");
        } else {
            return "redirect:/backendLogin";
        }
        final ForestResponse<ResultStatus<List<LeaderResponseDTO>>> allLeader = leaderApi.findAllLeader();
        if ("C000".equals(allLeader.getResult().getCode())) {
            final List<LeaderDTO> leaderDTOList = allLeader.getResult().getData()
                    .stream()
                    .filter(l -> StringUtils.isBlank(l.getDeletionDate()))
                    .map(ManagementController::constructLeaderDTO).collect(Collectors.toList());
            model.addAttribute("leaderList", leaderDTOList);
        }
        return "backend/leaderManagement";
    }


    /**
     * 新增/修改團主
     *
     * @param session
     * @return
     */
    @PostMapping("/saveLeader")
    public ResponseEntity<?> saveLeader(@RequestBody LeaderRequestDTO dto, HttpSession session) {
        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[saveLeader]");
        }
        ResultStatus resultStatus = new ResultStatus();

        if (StringUtils.isBlank(dto.getId())) { // 新增
            final ForestResponse<ResultStatus<?>> saveLeader = leaderApi.saveLeader(dto);
            resultStatus.setCode(saveLeader.getResult().getCode());
            resultStatus.setMessage(saveLeader.getResult().getMessage());
        } else {    // 修改
            final ForestResponse<ResultStatus<?>> editLeader = leaderApi.editLeader(dto);
            resultStatus.setCode(editLeader.getResult().getCode());
            resultStatus.setMessage(editLeader.getResult().getMessage());
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

        final ForestResponse<ResultStatus<?>> deleteLeader = leaderApi.deleteLeader(id);
        resultStatus.setCode(deleteLeader.getResult().getCode());
        resultStatus.setMessage(deleteLeader.getResult().getMessage());
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

        final ForestResponse<ResultStatus<List<LeaderResponseDTO>>> leaderSearch = leaderApi.leaderSearch(searchDTO);
        if ("C000".equals(leaderSearch.getResult().getCode())) {
            List<LeaderDTO> leaderDTOList = leaderSearch.getResult().getData().stream()
                    .map(ManagementController::constructLeaderDTO)
                    .collect(Collectors.toList());

            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(leaderDTOList);
        }

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 查詢單一團主資訊
     *
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/findLeaderById/{id}")
    public ResponseEntity<?> findLeaderById(@PathVariable String id, HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[findLeaderById]");
        }

        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<LeaderResponseDTO>> leaderInfo = leaderApi.getLeaderInfoById(id);
        resultStatus.setCode(leaderInfo.getResult().getCode());
        resultStatus.setMessage(leaderInfo.getResult().getMessage());
        resultStatus.setData(leaderInfo.getResult().getData());

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 導貼文管理頁面
     *
     * @param session
     * @param model
     * @return
     */
    @GetMapping("/forwardPostManagement")
    public String forwardPostManagement(HttpSession session, Model model) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[forwardPostManagement]");
        } else {
            return "redirect:/backendLogin";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfThreeDaysLater = now.withHour(23).withMinute(59).withSecond(59).withNano(0);
        String startOfTodayFormatted = startOfToday.format(ddf);
        String endOfTodayFormatted = endOfThreeDaysLater.format(ddf);

        final ForestResponse<ResultStatus<List<PostResponseDTO>>> todayNewPosts = postApi.todayNewPosts(startOfTodayFormatted, endOfTodayFormatted);
        if ("C000".equals(todayNewPosts.getResult().getCode())) {
            final List<PostDTO> postDTOList = todayNewPosts.getResult().getData()
                    .stream()
                    .map(post -> {
                        try {
                            return constructPostDTO(post);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());

            model.addAttribute("postDTOList", postDTOList);
        }

        //取得所有球館資訊，配對用
        final ForestResponse<ResultStatus<List<CourtResponseDTO>>> allCourt = courtApi.getAllCourt();
        if ("C000".equals(allCourt.getResult().getCode())) {
            model.addAttribute("courtList", allCourt.getResult().getData());
        } else {
            model.addAttribute("courtList", new ArrayList<CourtResponseDTO>());
        }

        return "backend/postManagement";
    }


    /**
     * 新增/修改貼文
     *
     * @param session
     * @return
     */
    @PostMapping("/savePost")
    public ResponseEntity<?> savePost(@RequestBody PostRequestDTO dto, HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[savePost]");
        }

        ResultStatus resultStatus = new ResultStatus();

        if (StringUtils.isBlank(dto.getId())) { //新增
            final ForestResponse<ResultStatus<?>> savePost = postApi.savePost(dto);
            resultStatus.setCode(savePost.getResult().getCode());
            resultStatus.setMessage(savePost.getResult().getMessage());
        } else {    //修改
            final ForestResponse<ResultStatus<?>> editPost = postApi.editPost(dto);
            resultStatus.setCode(editPost.getResult().getCode());
            resultStatus.setMessage(editPost.getResult().getMessage());
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

        final ForestResponse<ResultStatus<?>> deletePost = postApi.deletePost(id);
        resultStatus.setCode(deletePost.getResult().getCode());
        resultStatus.setMessage(deletePost.getResult().getMessage());
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

        final ForestResponse<ResultStatus<?>> deleteSelectedPost = postApi.deleteSelectedPost(postIdList);
        resultStatus.setCode(deleteSelectedPost.getResult().getCode());
        resultStatus.setMessage(deleteSelectedPost.getResult().getMessage());
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 查詢貼文
     *
     * @param searchDTO
     * @param session
     * @return
     */
    @PostMapping("/postSearch")
    public ResponseEntity<?> postSearch(@RequestBody SearchDTO searchDTO, HttpSession session) throws ParseException {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[postSearch]");
        }

        ResultStatus resultStatus = new ResultStatus();

        searchDTO.setStartDate(searchDTO.getStartDate() + " 00:00:00");
        searchDTO.setEndDate(searchDTO.getEndDate() + " 23:59:59");
        final ForestResponse<List<PostResponseDTO>> searchPosts = postApi.searchPosts(searchDTO);
        List<PostDTO> postDTOList = searchPosts.getResult().stream()
                .map(post -> {
                    try {
                        return constructPostDTO(post);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        if (!"All".equals(searchDTO.getLabelCourt()) && StringUtils.isNotBlank(searchDTO.getLabelCourt())) {
            postDTOList = postDTOList.stream().filter(p -> searchDTO.getLabelCourt().equals(p.getLabelCourt())).collect(Collectors.toList());
        }

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(postDTOList);

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 查詢單一貼文
     *
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/findPostById/{id}")
    public ResponseEntity<?> findPostById(@PathVariable String id, HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[findPostById]");
        }

        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<PostResponseDTO>> postInfo = postApi.onePostInfo(id);
        resultStatus.setCode(postInfo.getResult().getCode());
        resultStatus.setMessage(postInfo.getResult().getMessage());
        resultStatus.setData(postInfo.getResult().getData());
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 查找相同userId和startTime的Post
     *
     * @param session
     * @return
     */
    @GetMapping("/searchSamePosts")
    public ResponseEntity<?> searchSamePosts(HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[searchSamePosts]");
        }

        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<List<PostResponseDTO>>> samePosts = postApi.findSamePosts();

        final List<PostDTO> postDTOList = samePosts.getResult().getData().stream()
                .map(p -> {
                    PostDTO postDTO;
                    try {
                        postDTO = constructPostDTO(p);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    return postDTO;
                }).collect(Collectors.toList());

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(postDTOList);

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 自動配對球館與貼文的功能
     *
     * @param session
     * @return
     */
    @PostMapping("/autoMate")
    public ResponseEntity<?> autoMate(HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[matePostAndCourt]");
        }
        ResultStatus resultStatus = new ResultStatus();


        final ForestResponse<ResultStatus<List<PostResponseDTO>>> todayNewPosts = postApi.todayNewPosts(ssf.format(new Date()) + " 00:00:00", ssf.format(new Date()) + " 23:59:59");

        Map<String, List<String>> placeToIds = todayNewPosts.getResult().getData().stream().collect(Collectors.toMap(
                PostResponseDTO::getPlace,
                post -> {
                    List<String> idList = new ArrayList<>();
                    idList.add(post.getId());
                    return idList;
                },
                (existingIds, newIds) -> {
                    List<String> ids = new ArrayList<>(existingIds);
                    ids.addAll(newIds);
                    return ids;
                }
        ));

        final ForestResponse<ResultStatus<List<CourtResponseDTO>>> allCourt = courtApi.getAllCourt();

        List<CourtResponseDTO> courtList = allCourt.getResult().getData();
        courtList = courtList.stream()
                .map(c -> {
                    //取得球館既有的貼文ids
                    List<String> postIds = Arrays.asList(c.getPostId().split(","));
                    //查出既有貼文細節資訊
                    final ForestResponse<ResultStatus<List<PostResponseDTO>>> postList = postApi.findPostByIdIn(postIds);
                    final List<PostResponseDTO> posts = postList.getResult().getData();
                    //把新貼文id根據place歸檔到所屬的球館的postId屬性
                    if (posts != null && !posts.isEmpty()) {

                        Set<String> places = posts.stream()
                                .map(PostResponseDTO::getPlace)
                                .collect(Collectors.toSet());

                        //執行比對
                        String newIds = places.stream()
                                .flatMap(p -> placeToIds.getOrDefault(p, Collections.emptyList()).stream())
                                .collect(Collectors.joining(","));

                        // 更新postId
                        String currentPostId = c.getPostId();
                        if (currentPostId != null && !currentPostId.isEmpty()) {
                            if (!currentPostId.endsWith(",")) {
                                currentPostId += ",";
                            }
                            c.setPostId(currentPostId + newIds);
                        } else {
                            c.setPostId(newIds);
                        }

                        //更新貼文的labelCourt屬性
                        postApi.saveAllMatedPosts(new HashSet<>(Arrays.asList(newIds.split(","))));
                    }
                    return c;
                }).collect(Collectors.toList());

        courtApi.saveAllCourts(courtList);

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");

        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/notMatchedPosts")
    public ResponseEntity<?> notMatchedPosts(HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[notMatchedPosts]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<List<PostResponseDTO>>> todayNewPosts = postApi.todayNewPosts(ssf.format(new Date()) + " 00:00:00", ssf.format(new Date()) + " 23:59:59");
        final Set<String> userIdList = todayNewPosts.getResult().getData().stream().map(PostResponseDTO::getUserId).collect(Collectors.toSet());
        LeaderUserIdListDTO dto = new LeaderUserIdListDTO();
        dto.setUserIdList(userIdList);
        final ForestResponse<ResultStatus<List<LeaderResponseDTO>>> allLeaderByUserIds = leaderApi.getAllLeaderByUserIds(dto);
        final Map<String, LeaderResponseDTO> leaderMap = allLeaderByUserIds.getResult().getData()
                .stream()
                .collect(Collectors.toMap(LeaderResponseDTO::getUserId, Function.identity()));

        final List<PostDTO> todayPostDTOsNotYetMatched = todayNewPosts.getResult().getData()
                .stream().filter(p -> !p.isLabelCourt())
                .map(post -> {
                    try {
                        return constructPostDTO(leaderMap, post);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(todayPostDTOsNotYetMatched);

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 配對貼文與球館，將貼文id存入球館entity，以及在貼文標記labelCourt
     *
     * @param dto
     * @param session
     * @return
     */
    @PostMapping("/matePostsAndCourt")
    public ResponseEntity<?> matePostsAndCourt(@RequestBody MateDTO dto, HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[matePostAndCourt]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<CourtResponseDTO>> oneCourtInfo = courtApi.getCourtInfoById(dto.getCourtId());
        final CourtResponseDTO court = oneCourtInfo.getResult().getData();
        Set<String> uniquePostId = Arrays.stream(court.getPostId().split(","))
                .filter(id -> !id.trim().isEmpty()).collect(Collectors.toSet());
        uniquePostId.addAll(dto.getMatePostIds());
        final String postId = uniquePostId.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        court.setPostId(postId);
        courtApi.saveCourt(court);

        postApi.saveAllMatedPosts(uniquePostId);
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 篩選出費用不符合標準的貼文
     *
     * @param session
     * @return
     */
    @GetMapping("/issueFee")
    public ResponseEntity<?> issueFee(HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[issueFee]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<List<PostResponseDTO>>> todayNewPosts = postApi.todayNewPosts(ssf.format(new Date()) + " 00:00:00", ssf.format(new Date()) + " 23:59:59");
        final Set<String> userIdList = todayNewPosts.getResult().getData()
                .stream().map(PostResponseDTO::getUserId).collect(Collectors.toSet());
        List<PostResponseDTO> tempTodayPosts = todayNewPosts.getResult().getData().stream()
                .filter(p -> (p.getFee() / p.getDuration()) < 1.25 || (p.getFee() / p.getDuration()) > 2.09)
                .collect(Collectors.toList());

        LeaderUserIdListDTO dto = new LeaderUserIdListDTO();
        dto.setUserIdList(userIdList);
        final ForestResponse<ResultStatus<List<LeaderResponseDTO>>> leaderList = leaderApi.getAllLeaderByUserIds(dto);
        final Map<String, LeaderResponseDTO> leaderMap = leaderList.getResult().getData()
                .stream()
                .collect(Collectors.toMap(LeaderResponseDTO::getUserId, Function.identity()));

        final List<PostDTO> todayNewPostList = tempTodayPosts
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
     * 導檔案管理頁面
     *
     * @param session
     * @param model
     * @return
     */
    @GetMapping("/forwardFileManagement")
    public String forwardFileManagement(HttpSession session, Model model) throws IOException {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[forwardFileManagement]");
        } else {
            return "redirect:/backendLogin";
        }

        final List<RequestPostDTO> postDTOList = constructPostDTOFromDailyPostsFile(svf.format(new Date()));
        model.addAttribute("postDTOList", postDTOList);

        try {
            Pattern pattern = Pattern.compile("(\\d+)-data_disposable\\.json");
            List<String> sortedDateList = Files.walk(Paths.get(configProperties.getFileSavePath()))
                    .filter(Files::isRegularFile)
                    .map(path -> pattern.matcher(path.getFileName().toString()))
                    .filter(Matcher::matches)
                    .map(matcher -> matcher.group(1))
                    .sorted((s1, s2) -> Integer.compare(Integer.parseInt(s2), Integer.parseInt(s1))) // 按數字大小降冪排序
                    .distinct() // 去除重複項
                    .map(d -> {
                        try {
                            return ssf.format(svf.parse(d));
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            model.addAttribute("sortedDateList", sortedDateList);

            model.addAttribute("today", ssf.format(new Date()));

            // 取出喂openAI的貼文結果列表
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
            LocalDateTime endOfTodayLater = now.withHour(23).withMinute(59).withSecond(59).withNano(0);
            String startOfTodayFormatted = startOfToday.format(ddf);
            String endOfTodayFormatted = endOfTodayLater.format(ddf);

            final ForestResponse<ResultStatus<List<PostResponseDTO>>> todayNewPosts = postApi.todayNewPosts(startOfTodayFormatted, endOfTodayFormatted);
            final List<PostDTO> todayNewPostList = todayNewPosts.getResult().getData()
                    .stream()
                    .map(post -> {
                        try {
                            return constructPostDTO(post);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());

            model.addAttribute("postData", todayNewPostList);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "backend/fileManagement";
    }


    /**
     * 查詢檔案
     *
     * @param dto
     * @param session
     * @return
     */
    @PostMapping("/searchFiles/{date}")
    public ResponseEntity<?> searchFiles(@RequestBody SearchDTO dto, @PathVariable String date, HttpSession session) throws IOException, ParseException {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[searchFiles]");
        }

        ResultStatus resultStatus = new ResultStatus();

        date = svf.format(ssf.parse(date)); // 日期更換回yyyyMMdd

        List<RequestPostDTO> postDTOList = constructPostDTOFromDailyPostsFile(date);

        postDTOList = postDTOList.stream()
                .filter(p -> p.getName().contains(dto.getKeyword()) ||
                        p.getPostContent().contains(dto.getKeyword()))
                .collect(Collectors.toList());

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(postDTOList);

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 修改檔案內的貼文內容
     *
     * @param dto
     * @param session
     * @return
     * @throws IOException
     */
    @PostMapping("/editFile/{date}")
    public ResponseEntity<?> editFile(@RequestBody RequestPostDTO dto, @PathVariable String date, HttpSession session) throws IOException, ParseException {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[editFile]");
        }

        ResultStatus resultStatus = new ResultStatus();

        date = svf.format(ssf.parse(date)); // 日期更換回yyyyMMdd

        List<RequestPostDTO> postDTOList = constructPostDTOFromDailyPostsFile(date);
        postDTOList = postDTOList.stream()
                .map(p -> {
                    if (p.getUserId().equals(dto.getUserId())) {
                        p.setPostContent(dto.getPostContent());
                    }
                    return p;
                })
                .collect(Collectors.toList());

        File file = new File(configProperties.getFileSavePath() + date + "-data_disposable.json");
        objectMapper.writeValue(file, postDTOList);

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 刪除檔案
     *
     * @param userId
     * @param session
     * @return
     * @throws IOException
     */
    @GetMapping("/deleteFile/{userId}/{date}")
    public ResponseEntity<?> deleteFile(@PathVariable String userId, @PathVariable String date, HttpSession session) throws IOException, ParseException {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[deleteFile]");
        }

        ResultStatus resultStatus = new ResultStatus();

        date = svf.format(ssf.parse(date)); // 日期更換回yyyyMMdd

        List<RequestPostDTO> postDTOList = constructPostDTOFromDailyPostsFile(date);
        postDTOList = postDTOList.stream()
                .filter(p -> !p.getUserId().equals(userId))
                .collect(Collectors.toList());

        File file = new File(configProperties.getFileSavePath() + date + "-data_disposable.json");
        objectMapper.writeValue(file, postDTOList);

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 取得單筆零打資訊的檔案內文
     *
     * @param date
     * @param session
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @GetMapping("/showDisposableData/{date}")
    public ResponseEntity<?> showDisposableData(@PathVariable String date, HttpSession session) throws IOException, ParseException {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[showDisposableData]");
        }

        ResultStatus resultStatus = new ResultStatus();

        date = svf.format(ssf.parse(date)); // 日期更換回yyyyMMdd

        List<RequestPostDTO> postDTOList = constructPostDTOFromDailyPostsFile(date);

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(postDTOList);

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 透過唯一值userId取得file
     *
     * @param userId
     * @param date
     * @param session
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @GetMapping("/findFileByUserId/{userId}/{date}")
    public ResponseEntity<?> findFileByUserId(@PathVariable String userId, @PathVariable String date, HttpSession session) throws IOException, ParseException {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[findFileByUserId]");
        }

        ResultStatus resultStatus = new ResultStatus();

        date = svf.format(ssf.parse(date)); // 日期更換回yyyyMMdd

        List<RequestPostDTO> postDTOList = constructPostDTOFromDailyPostsFile(date);

        Optional<RequestPostDTO> optionalPost = postDTOList.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst();

        if (optionalPost.isPresent()) {
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(optionalPost.get());
        } else {
            resultStatus.setCode("C004");
            resultStatus.setMessage("查無檔案內貼文");
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 篩選出問題文章
     *
     * @param session
     * @return
     */
    @GetMapping("/searchIssues/{date}")
    public ResponseEntity<?> searchIssues(@PathVariable String date, HttpSession session) throws IOException, ParseException {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[searchIssues]");
        }

        ResultStatus resultStatus = new ResultStatus();

        date = svf.format(ssf.parse(date)); // 日期更換回yyyyMMdd

        String keywords = "教練|已滿|已額滿|場地出租|場地釋出|釋出|場地分享|場地轉讓|轉讓|場地轉租|徵場地|單打|售|練球|競標";
        Pattern issuePattern = Pattern.compile(keywords);

        List<RequestPostDTO> postDTOList = constructPostDTOFromDailyPostsFile(date);
        postDTOList = postDTOList.stream()
                .filter(p -> issuePattern.matcher(p.getPostContent()).find())
                .collect(Collectors.toList());

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("keywords", keywords);
        dataMap.put("postDTOList", postDTOList);

        resultStatus.setData(dataMap);

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 打OpenAI的text completion
     *
     * @param date
     * @param userIdList
     * @param session
     * @return
     * @throws ParseException
     * @throws IOException
     */
    @PostMapping("/convertToPosts/{date}")
    public CompletableFuture<ResponseEntity<ResultStatus>> convertToPosts(
            @PathVariable String date, @RequestBody List<String> userIdList, HttpSession session) throws ParseException, IOException {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[convertToPosts]");
        }

        ResultStatus resultStatus = new ResultStatus();

        try {
            date = svf.format(ssf.parse(date)); // 日期更換回yyyyMMdd

            List<RequestPostDTO> requestPostDTOList = constructPostDTOFromDailyPostsFile(date);
            requestPostDTOList = requestPostDTOList.stream()
                    .filter(p -> userIdList.contains(p.getUserId()))
                    .collect(Collectors.toList());

            if (requestPostDTOList.size() > 0) {

                List<RequestPostDTO> finalRequestPostDTOList = requestPostDTOList;
                return CompletableFuture.supplyAsync(() -> {
                    ForestResponse<ResultStatus<List<PostResponseDTO>>> response = openaiApi.convertToPosts(finalRequestPostDTOList);

                    if (response.getResult() == null || response.getResult().getData() == null) {
                        resultStatus.setCode("C999");
                        resultStatus.setMessage("無法取得資料");
                        return ResponseEntity.ok(resultStatus);
                    }

                    // 轉換 PostResponseDTO 為 PostDTO
                    final List<PostDTO> postDTOList = response.getResult().getData()
                            .stream().map(p -> {
                                try {
                                    return constructPostDTO(p);
                                } catch (ParseException e) {
                                    throw new RuntimeException("轉換失敗", e);
                                }
                            }).collect(Collectors.toList());

                    // 返回成功結果
                    resultStatus.setCode("C000");
                    resultStatus.setMessage("成功");
                    resultStatus.setData(postDTOList);
                    return ResponseEntity.ok(resultStatus);
                }).exceptionally(ex -> {
                    resultStatus.setCode("C999");
                    resultStatus.setMessage("例外發生: " + ex.getMessage());
                    return ResponseEntity.ok(resultStatus);
                });
            }
        } catch (ParseException | IOException e) {
            resultStatus.setCode("C999");
            resultStatus.setMessage("例外發生: " + e.getMessage());
            return CompletableFuture.completedFuture(ResponseEntity.ok(resultStatus));
        }

        return CompletableFuture.completedFuture(ResponseEntity.ok(resultStatus)); // 這裡返回的 resultStatus 將處於非正常狀態
    }



    /**
     * 取得尚未匯入DB的零打資訊
     *
     * @param date
     * @param session
     * @return
     * @throws ParseException
     * @throws IOException
     */
    @GetMapping("/getUnfinishedFiles/{date}")
    public ResponseEntity<?> getUnfinishedFiles(@PathVariable String date, HttpSession session) throws ParseException, IOException {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[findMissingFiles]");
        }

        ResultStatus resultStatus = new ResultStatus();

        date = svf.format(ssf.parse(date)); // 日期更換回yyyyMMdd

        List<RequestPostDTO> requestPostDTOList = constructPostDTOFromDailyPostsFile(date);

        final ForestResponse<ResultStatus<List<PostResponseDTO>>> todayNewPosts = postApi.todayNewPosts(ssf.format(new Date()) + " 00:00:00", ssf.format(new Date()) + " 23:59:59");

        final List<String> savedUserIdList = todayNewPosts.getResult().getData()
                .stream().map(PostResponseDTO::getUserId).collect(Collectors.toList());

        requestPostDTOList = requestPostDTOList.stream()
                .filter(p -> !savedUserIdList.contains(p.getUserId()))
                .collect(Collectors.toList());

        if (requestPostDTOList.size() > 0) {
            resultStatus.setCode("C005");
            resultStatus.setMessage("尚有檔案未匯入資料庫");
            resultStatus.setData(requestPostDTOList);
        } else {
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        }

        return ResponseEntity.ok(resultStatus);
    }



    /**
     * 新增球館
     * @param dto
     * @param session
     * @return
     */
    @PostMapping("/saveCourt")
    public ResponseEntity<?> saveCourt(@RequestBody CourtRequestDTO dto, HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[saveCourt]");
        }
        ResultStatus resultStatus = new ResultStatus();

        CourtResponseDTO responseDTO = new CourtResponseDTO();
        responseDTO.setAddress(dto.getAddress());
        responseDTO.setName(dto.getName());
        responseDTO.setLongitude(dto.getLongitude());
        responseDTO.setLatitude(dto.getLatitude());
        responseDTO.setPostId(dto.getPostId());
        responseDTO.setId(dto.getId());

        final ForestResponse<ResultStatus<?>> saveCourt = courtApi.saveCourt(responseDTO);
        resultStatus.setCode(saveCourt.getResult().getCode());
        resultStatus.setMessage(saveCourt.getResult().getMessage());
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 球館模糊查詢
     * @param searchDTO
     * @param session
     * @return
     */
    @PostMapping("/searchCourt")
    public ResponseEntity<?> searchCourt(@RequestBody SearchDTO searchDTO, HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[searchCourt]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<List<CourtResponseDTO>>> courtList = courtApi.searchCourt(searchDTO);
        final List<CourtDTO> courtDTOList = courtList.getResult().getData()
                .stream().map(this::constructCourtDTOSimple).collect(Collectors.toList());

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(courtDTOList);
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 用id找球館
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/findCourtById/{id}")
    public ResponseEntity<?> findCourtById(@PathVariable String id, HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[findCourtById]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<CourtResponseDTO>> oneCourtInfo = courtApi.getCourtInfoById(id);
        resultStatus.setCode(oneCourtInfo.getResult().getCode());
        resultStatus.setMessage(oneCourtInfo.getResult().getMessage());
        resultStatus.setData(constructCourtDTO(oneCourtInfo.getResult().getData()));
        return ResponseEntity.ok(resultStatus);
    }


    private CourtDTO constructCourtDTO(CourtResponseDTO court) {
        CourtDTO courtDTO = new CourtDTO();
        courtDTO.setAddress(court.getAddress());
        courtDTO.setLatitude(court.getLatitude());
        courtDTO.setLongitude(court.getLongitude());
        courtDTO.setName(court.getName());
        courtDTO.setId(court.getId());
        courtDTO.setCreationDate(court.getCreationDate());
        List<String> postIdsList = (court.getPostId() == null || court.getPostId().trim().isEmpty())
                ? Collections.emptyList()
                : Arrays.asList(court.getPostId().split(","));
        final ForestResponse<ResultStatus<List<PostResponseDTO>>> postList = postApi.findPostByIdIn(postIdsList);
        final List<PostDTO> postDTOList = postList.getResult().getData()
                .stream().map(p -> {
                    try {
                        return constructPostDTO(p);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
        courtDTO.setPostDTOList(postDTOList);
        return courtDTO;
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


    private PostDTO constructPostDTO(PostResponseDTO post) throws ParseException {
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


    private CourtDTO constructCourtDTOSimple(CourtResponseDTO court) {
        CourtDTO courtDTO = new CourtDTO();
        courtDTO.setAddress(court.getAddress());
        courtDTO.setLatitude(court.getLatitude());
        courtDTO.setLongitude(court.getLongitude());
        courtDTO.setName(court.getName());
        courtDTO.setId(court.getId());
        courtDTO.setCreationDate(court.getCreationDate());
        courtDTO.setPostId(court.getPostId());
        return courtDTO;
    }
}

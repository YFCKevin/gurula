package com.yfckevin.badminton.controller;

import com.yfckevin.badminton.dto.CourtDTO;
import com.yfckevin.badminton.dto.NearByRequestDTO;
import com.yfckevin.badminton.dto.PostDTO;
import com.yfckevin.badminton.dto.SearchDTO;
import com.yfckevin.badminton.entity.Court;
import com.yfckevin.badminton.entity.Leader;
import com.yfckevin.badminton.entity.Post;
import com.yfckevin.badminton.service.CourtService;
import com.yfckevin.badminton.service.LeaderService;
import com.yfckevin.badminton.service.PostService;
import com.yfckevin.common.exception.ResultStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class CourtController {
    private final CourtService courtService;
    private final PostService postService;
    private final LeaderService leaderService;
    private static final double EARTH_RADIUS = 6371; // 地球半徑，單位是公里
    private final DateTimeFormatter ddf;
    private final SimpleDateFormat sdf;
    Logger logger = LoggerFactory.getLogger(CourtController.class);
    public CourtController(CourtService courtService, PostService postService, LeaderService leaderService, DateTimeFormatter ddf, @Qualifier("sdf") SimpleDateFormat sdf) {
        this.courtService = courtService;
        this.postService = postService;
        this.leaderService = leaderService;
        this.ddf = ddf;
        this.sdf = sdf;
    }

    @GetMapping("/getCourtInfoByPostId/{postId}")
    public ResponseEntity<?> getCourtInfoByPostId (@PathVariable String postId){
        logger.info("[getCourtInfoById]");
        ResultStatus resultStatus = new ResultStatus();
        final Optional<Court> opt = courtService.findByPostId(postId);
        if (opt.isEmpty()) {
            resultStatus.setCode("C008");
            resultStatus.setMessage("查無球場");
        } else {
            final Court court = opt.get();
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(court);
        }
        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/getCourtInfoById/{id}")
    public ResponseEntity<?> getCourtInfoById (@PathVariable String id){
        logger.info("[getCourtInfoById]");
        ResultStatus resultStatus = new ResultStatus();
        final Optional<Court> opt = courtService.findById(id);
        if (opt.isEmpty()) {
            resultStatus.setCode("C008");
            resultStatus.setMessage("查無球場");
        } else {
            final Court court = opt.get();
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(court);
        }
        return ResponseEntity.ok(resultStatus);
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

        List<Court> courtList = courtService.findAllByOrderByCreationDateAsc();
        List<Court> nearbyCourts = new ArrayList<>();
        for (Court court : courtList) {
            final double distance = calculateDistance(dto.getMyLat(), dto.getMyLon(), court.getLatitude(), court.getLongitude());

            if (distance <= dto.getRadius()) {
                nearbyCourts.add(court);
            }
        }

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(nearbyCourts);

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

        courtService.findById(id)
                .map(c -> {
                    final List<String> postIdList = Arrays.asList(c.getPostId().split(","));
                    LocalDateTime today = LocalDate.now().atStartOfDay();   //今日00:00:00
                    final List<Post> postList = postService.findByIdIn(postIdList).stream()
                            .filter(post -> LocalDateTime.parse(post.getStartTime(), ddf).isEqual(today) || LocalDateTime.parse(post.getStartTime(), ddf).isAfter(today))
                            .collect(Collectors.toList());
                    final Set<String> userIdList = postList.stream().map(Post::getUserId).collect(Collectors.toSet());

                    final List<Leader> leaderList = leaderService.findAllByUserIdIn(userIdList);

                    Map<String, Object> result = new HashMap<>();
                    result.put("leaderList", leaderList);
                    result.put("postList", postList);

                    resultStatus.setCode("C000");
                    resultStatus.setMessage("成功");
                    resultStatus.setData(result);
                    return resultStatus;
                })
                .orElseGet(() -> {
                    resultStatus.setCode("C008");
                    resultStatus.setMessage("查無球場");
                    return resultStatus;
                });

        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/getAllCourt")
    public ResponseEntity<?> getAllCourt (){
        logger.info("[getAllCourt]");
        ResultStatus resultStatus = new ResultStatus();
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(courtService.findAllByOrderByCreationDateAsc());
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/saveAllCourts")
    public ResponseEntity<?> saveAllCourts (@RequestBody List<Court> courtList){
        logger.info("[saveAllCourts]");
        ResultStatus resultStatus = new ResultStatus();
        courtService.saveAll(courtList);
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/saveCourt")
    public ResponseEntity<?> saveCourt(@RequestBody CourtDTO dto, HttpSession session) {

        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[saveCourt]");
        }
        ResultStatus resultStatus = new ResultStatus();

        if (StringUtils.isBlank(dto.getId())) { //新增
            Court court = new Court();
            court.setName(dto.getName());
            court.setAddress(dto.getAddress());
            court.setLatitude(dto.getLatitude());
            court.setLongitude(dto.getLongitude());
            court.setCreationDate(sdf.format(new Date()));
            court.setPostId(dto.getPostId());
            courtService.save(court);
        } else {    //更新
            final Optional<Court> opt = courtService.findById(dto.getId());
            if (opt.isPresent()) {
                final Court court = opt.get();
                court.setLongitude(dto.getLongitude());
                court.setName(dto.getName());
                court.setAddress(dto.getAddress());
                court.setLatitude(dto.getLatitude());
                court.setPostId(dto.getPostId());
                courtService.save(court);
            }
        }
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        return ResponseEntity.ok(resultStatus);
    }


    @CrossOrigin(origins = "https://geo-tw.zeabur.app")
    @GetMapping("/allCourtAndPost")
    public  ResponseEntity<?> allCourtAndPost(){
        final List<Court> courtList = courtService.findAllByOrderByCreationDateAsc();
        final Map<String, List<Post>> postMap = courtList.stream()
                .collect(Collectors.toMap(
                        Court::getId,
                        court -> Arrays.stream(court.getPostId().split(","))
                                .map(postId -> postService.findById(postId).orElse(null))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                ));
        List<String> postIdList = courtList.stream().map(court -> court.getPostId().split(","))
                .flatMap(Arrays::stream)
                .collect(Collectors.toCollection(ArrayList::new));
        LocalDateTime today = LocalDate.now().atStartOfDay();   //今日00:00:00
        final List<Post> postList = postService.findByIdIn(postIdList).stream()
                .filter(post -> LocalDateTime.parse(post.getStartTime(), ddf).isEqual(today) || LocalDateTime.parse(post.getStartTime(), ddf).isAfter(today))
                .collect(Collectors.toList());
        final Set<String> userIdList = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
        final Map<String, Leader> leaderMap = leaderService.findAllByUserIdIn(userIdList)
                .stream()
                .collect(Collectors.toMap(Leader::getUserId, Function.identity()));

        final List<CourtDTO> courtDTOList = courtList
                .stream()
                .map(court -> {
                    CourtDTO dto = new CourtDTO();
                    final List<PostDTO> postDTOList = postMap.getOrDefault(court.getId(), new ArrayList<>())
                            .stream()
                            .map(p -> {
                                try {
                                    return constructPostDTO(leaderMap, p);
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }).collect(Collectors.toList());
                    dto.setPostDTOList(postDTOList);
                    dto.setCreationDate(court.getCreationDate());
                    dto.setName(court.getName());
                    dto.setLatitude(court.getLatitude());
                    dto.setLongitude(court.getLongitude());
                    dto.setAddress(court.getAddress());
                    dto.setId(court.getId());
                    return dto;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(courtDTOList);
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

        final List<Court> courtList = courtService.findCourtByCondition(searchDTO.getKeyword().trim());

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(courtList);
        return ResponseEntity.ok(resultStatus);
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


    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c; // 距離，單位是公里
    }
}

package com.yfckevin.lineservice.controller;

import com.dtflys.forest.http.ForestResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yfckevin.common.dto.line.LineUserProfileResponseDTO;
import com.yfckevin.common.dto.line.LineWebhookRequestDTO;
import com.yfckevin.lineservice.ConfigProperties;
import com.yfckevin.lineservice.api.LineApi;
import com.yfckevin.lineservice.entity.Follower;
import com.yfckevin.common.exception.ResultStatus;
import com.yfckevin.lineservice.service.FollowerService;
import com.yfckevin.lineservice.service.LineService;
import com.yfckevin.lineservice.utils.FlexMessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class LineController {
    private final ConfigProperties configProperties;
    private final LineService lineService;
    private final FollowerService followerService;
    private final FlexMessageUtil flexMessageUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpleDateFormat sdf;
    private final SimpleDateFormat ssf;
    private final LineApi lineApi;
    Logger logger = LoggerFactory.getLogger(LineController.class);

    public LineController(ConfigProperties configProperties, LineService lineService, FollowerService followerService, FlexMessageUtil flexMessageUtil, RedisTemplate<String, String> redisTemplate, @Qualifier("sdf") SimpleDateFormat sdf, @Qualifier("ssf") SimpleDateFormat ssf, LineApi lineApi) {
        this.configProperties = configProperties;
        this.lineService = lineService;
        this.followerService = followerService;
        this.flexMessageUtil = flexMessageUtil;
        this.redisTemplate = redisTemplate;
        this.sdf = sdf;
        this.ssf = ssf;
        this.lineApi = lineApi;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(@RequestBody LineWebhookRequestDTO dto) throws JsonProcessingException, ParseException {

        logger.info("[lines取得使用者的訊息]");

        ResultStatus resultStatus = new ResultStatus();

        String msg = "{\n" +
                "  \"type\": \"text\",\n" +
                "  \"text\": \"感謝您的訊息！\\n本系統為自動回覆功能，\\n稍後盡快回覆您訊息！\"\n" +
                "}";
        // 訊息存入redis
        Map<String, String> userData = new HashMap<>();
        for (LineWebhookRequestDTO.Event event : dto.getEvents()) {
            final String userId = event.getSource().getUserId();
            logger.info("事件類型：{}, 發送者：{}", event.getType(), userId);

            userData.put("channelID", dto.getDestination());
            userData.put("eventType", String.valueOf(event.getType()));
            if (event.getMessage() != null) {
                switch (event.getMessage().getType()) {
                    case text:
                        userData.put("messageText", event.getMessage().getText());
                        userData.put("messageType", String.valueOf(event.getMessage().getType()));
                        userData.put("messageId", event.getMessage().getId());
                        userData.put("messageQuoteToken", event.getMessage().getQuoteToken());
                        break;

                    case audio:
                    case video:
                        userData.put("duration", String.valueOf(event.getMessage().getDuration()));
                        break;

                    case sticker:
                        userData.put("stickerId", event.getMessage().getStickerId());
                        userData.put("packageId", event.getMessage().getPackageId());
                        userData.put("stickerResourceType", String.valueOf(event.getMessage().getStickerResourceType()));
                        break;

                    case location:
                        userData.put("latitude", String.valueOf(event.getMessage().getLatitude()));
                        userData.put("longitude", String.valueOf(event.getMessage().getLongitude()));
                        userData.put("address", event.getMessage().getAddress());
                        break;
                }
            }

            userData.put("redelivery", String.valueOf(event.getDeliveryContext().isRedelivery()));
            userData.put("sourceType", event.getSource().getType());
            userData.put("sourceUserId", userId);
            userData.put("webhookEventId", event.getWebhookEventId());
            userData.put("timestamp", String.valueOf(event.getTimestamp()));

            switch (event.getType()) {
                case message: {
                    userData.put("replyToken", event.getReplyToken());
                    if ("我要找今天的零打團".equals(event.getMessage().getText())) {
                        logger.info("列出今天的零打團");
                        msg = "{\n" +
                                "  \"type\": \"text\",\n" +
                                "  \"text\": \"很開心能為您服務！提供您今日零打資訊～～～\\n\\n" +
                                "LINE系統最多提供10則零打資訊\\n" +
                                "要查看更多零打資訊歡迎前往：https://gurula.cc/badminton/posts\"" +
                                "}";


                        //推送圖文輪詢
                        final String startDate = ssf.format(new Date()) + " 00:00:00";
                        String endDate = ssf.format(new Date()) + " 23:59:59";
                        final Map<String, Object> imageCarouselTemplate = flexMessageUtil.assembleImageCarouselTemplate(startDate, endDate);
                        if ("查無零打資訊".equals(imageCarouselTemplate.get("error"))) {

                            msg = "{\n" +
                                    "  \"type\": \"text\",\n" +
                                    "  \"text\": \"很抱歉！目前今日沒有零打團Q_Q\\n\\n" +
                                    "可以點擊「選擇零打日期」功能查詢其他日期的零打資訊\\n" +
                                    "若要查看更多資訊歡迎前往：https://gurula.cc/badminton/posts\"" +
                                    "}";

                        } else {
                            Map<String, Object> data = new HashMap<>();
                            data.put("to", event.getSource().getUserId());
                            data.put("messages", List.of(imageCarouselTemplate));
                            lineApi.pushOne(configProperties.getChannelAccessToken(), data);
                        }

                    } else if ("我要找某一天的零打團".equals(event.getMessage().getText())) {
                        logger.info("推送給user選擇日期");
                        msg = "{\n" +
                                "  \"type\": \"flex\",\n" +
                                "  \"altText\": \"請選擇日期\",\n" +
                                "  \"contents\": {\n" +
                                "    \"type\": \"bubble\",\n" +
                                "    \"body\": {\n" +
                                "      \"type\": \"box\",\n" +
                                "      \"layout\": \"vertical\",\n" +
                                "      \"contents\": [\n" +
                                "        {\n" +
                                "          \"type\": \"button\",\n" +
                                "          \"action\": {\n" +
                                "            \"type\": \"datetimepicker\",\n" +
                                "            \"label\": \"選擇日期\",\n" +
                                "            \"data\": \"action=selectDate\",\n" +
                                "            \"mode\": \"date\"\n" +
                                "          },\n" +
                                "          \"style\": \"primary\"\n" +
                                "        }\n" +
                                "      ]\n" +
                                "    }\n" +
                                "  }\n" +
                                "}";
                    } else if ("歐嗚Q_Q 當月免費額度用完了，何不來點打賞>口<".equals(event.getMessage().getText())) {
                        int packageId = 446; //   貼圖包 ID
                        int stickerId = 2027; // 貼圖 ID
                        String type = "sticker";

                        msg = "{"
                                + "\"type\":\"" + type + "\","
                                + "\"packageId\":" + packageId + ","
                                + "\"stickerId\":" + stickerId
                                + "}";
                    }
                    break;
                }
                case follow: {
                    logger.info("[follow]");
                    //取得該會員的基本資料
                    final ForestResponse<LineUserProfileResponseDTO> userProfileDTO = lineApi.getUserProfile(configProperties.getChannelAccessToken(), userId);
                    final LineUserProfileResponseDTO profileDTO = userProfileDTO.getResult();
                    Optional<Follower> followerOpt = followerService.findByUserId(profileDTO.getUserId());
                    Follower follower = null;
                    if (followerOpt.isPresent()) {  //追蹤者存在
                        follower = followerOpt.get();
                        follower.setUnfollowTime(null);
                        if (StringUtils.isNotBlank(follower.getChannelID())) {
                            follower.setChannelID(follower.getChannelID() + "," + dto.getDestination());
                        } else {
                            follower.setChannelID(dto.getDestination());
                        }
                    } else {    //第一次追蹤
                        follower = new Follower();
                        follower.setDisplayName(profileDTO.getDisplayName());
                        follower.setUserId(event.getSource().getUserId());
                        follower.setPictureUrl(profileDTO.getPictureUrl());
                        follower.setChannelID(dto.getDestination());
                    }
                    followerService.save(follower);

                    msg = "";
                    break;
                }
                case unfollow: {
                    logger.info("[unfollow]");
                    //取得該會員的基本資料
                    final ForestResponse<LineUserProfileResponseDTO> userProfileDTO = lineApi.getUserProfile(configProperties.getChannelAccessToken(), userId);
                    Optional<Follower> followerOpt = followerService.findByUserId(userProfileDTO.getResult().getUserId());
                    Follower follower = null;
                    if (followerOpt.isPresent()) {  //追蹤者存在
                        follower = followerOpt.get();
                        if (StringUtils.isNotBlank(follower.getChannelID())) {
                            List<String> channelIDList = Arrays.asList(follower.getChannelID().split(","));
                            String channelIDStr = channelIDList.stream()
                                    .filter(channelID -> !channelID.equals(dto.getDestination()))
                                    .collect(Collectors.joining(","));
                            follower.setChannelID(channelIDStr);
                        }
                        followerService.save(follower);
                    }
                    break;
                }
                case postback: {
                    logger.info("[postback]");
                    String postbackData = event.getPostback().getData();
                    if (postbackData.contains("action=selectDate")) {
                        logger.info("選擇日期是：" + event.getPostback().getParams().get("date"));
                        String selectedDate = event.getPostback().getParams().get("date") + " 00:00:00";
                        String endDate = event.getPostback().getParams().get("date") + " 23:59:59";
                        final Map<String, Object> imageCarouselTemplate = flexMessageUtil.assembleImageCarouselTemplate(selectedDate, endDate);
                        if ("查無零打資訊".equals(imageCarouselTemplate.get("error"))) {

                            msg = "{\n" +
                                    "  \"type\": \"text\",\n" +
                                    "  \"text\": \"很抱歉！" + event.getPostback().getParams().get("date") + " 目前沒有零打團Q_Q\\n\\n" +
                                    "可以再一次選擇其他日期查詢唷～\\n" +
                                    "若要查看更多資訊歡迎前往：https://gurula.cc/badminton/posts\"" +
                                    "}";

                        } else {
                            Map<String, Object> data = new HashMap<>();
                            data.put("to", event.getSource().getUserId());
                            data.put("messages", List.of(imageCarouselTemplate));
                            lineApi.pushOne(configProperties.getChannelAccessToken(), data);

                            msg = "{\n" +
                                    "  \"type\": \"text\",\n" +
                                    "  \"text\": \"很開心能為您服務！提供您 " + event.getPostback().getParams().get("date") + " 的零打資訊～～～\\n\\n" +
                                    "LINE系統最多提供10則零打資訊\\n" +
                                    "要查看更多資訊歡迎前往：https://gurula.cc/badminton/posts\"" +
                                    "}";
                        }
                    }
                    break;
                }
            }

            redisTemplate.opsForHash().putAll(event.getWebhookEventId(), userData);
            //創立索引
            redisTemplate.opsForZSet().add(userId, event.getWebhookEventId(), event.getTimestamp());
//            if (Boolean.TRUE.equals(redisTemplate.hasKey(event.getWebhookEventId()))) {
//                redisTemplate.expire(event.getWebhookEventId(), Duration.ofDays(7));
//            }
//            if (Boolean.TRUE.equals(redisTemplate.hasKey(userId))) {
//                redisTemplate.expire(userId, Duration.ofDays(7));
//            }

            lineService.autoReply(msg, event.getReplyToken());
        }

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(dto);

        return ResponseEntity.ok(resultStatus);
    }
}

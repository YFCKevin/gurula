package com.yfckevin.lineservice.controller;

import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.badminton.dto.badminton.SearchDTO;
import com.yfckevin.common.exception.ResultStatus;
import com.yfckevin.lineservice.ConfigProperties;
import com.yfckevin.lineservice.api.LineApi;
import com.yfckevin.lineservice.dto.*;
import com.yfckevin.lineservice.entity.Follower;
import com.yfckevin.lineservice.entity.TemplateDetail;
import com.yfckevin.lineservice.entity.TemplateSubject;
import com.yfckevin.lineservice.service.FollowerService;
import com.yfckevin.lineservice.service.TemplateDetailService;
import com.yfckevin.lineservice.service.TemplateSubjectService;
import com.yfckevin.lineservice.utils.FileUtils;
import com.yfckevin.lineservice.utils.FlexMessageUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class TemplateController {
    Logger logger = LoggerFactory.getLogger(TemplateController.class);
    private final LineApi lineApi;
    private final FlexMessageUtil flexMessageUtil;
    private final TemplateSubjectService templateSubjectService;
    private final TemplateDetailService templateDetailService;
    private final FollowerService followerService;
    private final ConfigProperties configProperties;
    private final SimpleDateFormat sdf;
    private final SimpleDateFormat picSuffix;
    private final FileUtils fileUtils;

    public TemplateController(LineApi lineApi, FlexMessageUtil flexMessageUtil, TemplateSubjectService templateSubjectService, TemplateDetailService templateDetailService, FollowerService followerService, ConfigProperties configProperties, @Qualifier("sdf") SimpleDateFormat sdf, SimpleDateFormat picSuffix, FileUtils fileUtils) {
        this.lineApi = lineApi;
        this.flexMessageUtil = flexMessageUtil;
        this.templateSubjectService = templateSubjectService;
        this.templateDetailService = templateDetailService;
        this.followerService = followerService;
        this.configProperties = configProperties;
        this.sdf = sdf;
        this.picSuffix = picSuffix;
        this.fileUtils = fileUtils;
    }

    @GetMapping("/getAllTemplate")
    public ResponseEntity<?> getAllTemplate() {
        logger.info("[getAllTemplate]");
        ResultStatus resultStatus = new ResultStatus();
        final List<TemplateSubject> templateSubjectList = templateSubjectService.findAllAndOrderByCreationDate();
//        List<TemplateSubjectResponseDTO> templateSubjectResponseDTOList = templateSubjectList.stream()
//                .map(TemplateController::constructTemplateSubjectResponseDTO)
//                .collect(Collectors.toList());
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(templateSubjectList);
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/getTemplateDetailByIdIn")
    public ResponseEntity<?> getTemplateDetailByIdIn (@RequestBody List<String> detailIdList){
        logger.info("[getTemplateDetailByIdIn]");
        ResultStatus resultStatus = new ResultStatus();
        final List<TemplateDetail> templateDetailList = templateDetailService.findByIdIn(detailIdList);
//        List<TemplateDetailResponseDTO> templateDetailResponseDTOList = templateDetailList.stream()
//                .map(TemplateController::constructTemplateDetailResponseDTO)
//                .collect(Collectors.toList());
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(templateDetailList);
        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/findAllFollower")
    public ResponseEntity<?> findAllFollower (){
        logger.info("[findAllFollower]");
        ResultStatus resultStatus = new ResultStatus();
        final List<Follower> followerList = followerService.findAll();
//        List<FollowerResponseDTO> followerResponseDTOList = followerList.stream()
//                .map(TemplateController::constructFollowerResponseDTO)
//                .collect(Collectors.toList());
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(followerList);
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/saveTemplateSubject")
    public ResponseEntity<?> saveTemplateSubject (@RequestBody TemplateSubjectRequestDTO requestDTO){
        logger.info("[saveTemplateSubject]");
        ResultStatus resultStatus = new ResultStatus();
        if (StringUtils.isBlank(requestDTO.getId())) {    //新增
            TemplateSubject templateSubject = new TemplateSubject();
            templateSubject.setTemplateType(requestDTO.getTemplateType());
            templateSubject.setUserIds(requestDTO.getUserIds());
            templateSubject.setTitle(requestDTO.getTitle());
            templateSubject.setSendDate(requestDTO.getSendDate());
            templateSubject.setAltText(requestDTO.getAltText());
            templateSubject.setDetailIds(requestDTO.getDetailIds());
            templateSubject.setCreationDate(sdf.format(new Date()));
            templateSubjectService.save(templateSubject);
        } else {    //更新
            final Optional<TemplateSubject> opt = templateSubjectService.findById(requestDTO.getId());
            final TemplateSubject templateSubject = opt.get();
            templateSubject.setTemplateType(requestDTO.getTemplateType());
            templateSubject.setUserIds(requestDTO.getUserIds());
            templateSubject.setTitle(requestDTO.getTitle());
            templateSubject.setSendDate(requestDTO.getSendDate());
            templateSubject.setAltText(requestDTO.getAltText());
            templateSubject.setDetailIds(requestDTO.getDetailIds());
            templateSubjectService.save(templateSubject);
        }
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        return ResponseEntity.ok(resultStatus);
    }
    
    
    @GetMapping("/oneTemplateSubjectInfo/{id}")
    public ResponseEntity<?> oneTemplateSubjectInfo (@PathVariable String id){
        logger.info("[saveTemplateSubject]");
        ResultStatus resultStatus = new ResultStatus();
        final Optional<TemplateSubject> opt = templateSubjectService.findById(id);
        if (opt.isEmpty()) {
            resultStatus.setCode("C010");
            resultStatus.setMessage("查無模板");
        } else {
            final TemplateSubject templateSubject = opt.get();
//            final TemplateSubjectResponseDTO dto = constructTemplateSubjectResponseDTO(templateSubject);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(templateSubject);
        }
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/searchFollower")
    public ResponseEntity<?> searchFollower (@RequestBody SearchDTO searchDTO){
        logger.info("[searchFollower]");
        ResultStatus resultStatus = new ResultStatus();

        List<Follower> followerList = followerService.searchFollower(searchDTO.getKeyword().trim());
//        List<FollowerResponseDTO> followerResponseDTOList = followerList.stream()
//                .map(TemplateController::constructFollowerResponseDTO)
//                .collect(Collectors.toList());
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(followerList);

        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/templateSearch")
    public ResponseEntity<?> templateSearch (@RequestBody SearchDTO searchDTO){
        logger.info("[templateSearch]");
        ResultStatus resultStatus = new ResultStatus();

        List<TemplateSubject> subjectList = templateSubjectService.templateSearch(searchDTO.getKeyword().trim());
//        List<TemplateSubjectResponseDTO> templateSubjectResponseDTOList = subjectList.stream()
//                        .map(TemplateController::constructTemplateSubjectResponseDTO)
//                .collect(Collectors.toList());
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(subjectList);
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/addTemplateDetail")
    public ResponseEntity<?> addTemplateDetail (@ModelAttribute TemplateDetailDTO dto) throws IOException {
        logger.info("[addTemplateDetail]");

        ResultStatus resultStatus = new ResultStatus();

        if (StringUtils.isBlank(dto.getId())) { //新增
            Path uploadPath = Path.of(configProperties.getPicSavePath());
            if (!Files.exists(uploadPath)) {
                Files.createDirectory(uploadPath);
            }

            TemplateDetail templateDetail = new TemplateDetail();

            final MultipartFile nameFile = dto.getMultipartFile();

            if (nameFile != null && !nameFile.isEmpty() && nameFile.getSize() != 0) {
                final String extension = FilenameUtils.getExtension(nameFile.getOriginalFilename());
                String fileName = picSuffix.format(new Date()) + "." + extension;
                System.out.println("fileName: " + fileName);

                try {
                    System.out.println("上傳圖片");
                    fileUtils.saveUploadedFile(nameFile, configProperties.getPicSavePath() + fileName);
                } catch (IOException e) {
                    System.out.println("圖片上傳失敗");
                    logger.error(e.getMessage(), e);
                    resultStatus.setCode("C013");
                    resultStatus.setMessage("圖片上傳失敗");
                    return ResponseEntity.ok(resultStatus);
                }
                templateDetail.setCover(configProperties.getPicShowPath() + fileName);
            }

            templateDetail.setMainTitle(dto.getMainTitle());
            templateDetail.setSubTitle(dto.getSubTitle());
            templateDetail.setTextContent(dto.getTextContent());
            templateDetail.setButtonName(dto.getButtonName());
            templateDetail.setButtonUrl(dto.getButtonUrl());
            templateDetail.setCreationDate(sdf.format(new Date()));
            TemplateDetail savedTemplateDetail = templateDetailService.save(templateDetail);

            //模板細節的id存入模板主題內
            Optional<TemplateSubject> opt = templateSubjectService.findById(dto.getSubjectId());
            if (opt.isPresent()) {
                TemplateSubject templateSubject = opt.get();
                final List<String> detailIds = templateSubject.getDetailIds();
                detailIds.add(savedTemplateDetail.getId());
                templateSubject.setDetailIds(detailIds);
                templateSubjectService.save(templateSubject);
            }
        }

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");

        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/oneTemplateDetailInfo/{id}")
    public ResponseEntity<?> oneTemplateDetailInfo (@PathVariable String id){
        logger.info("[oneTemplateDetailInfo]");
        ResultStatus resultStatus = new ResultStatus();
        final Optional<TemplateDetail> opt = templateDetailService.findById(id);
        if (opt.isEmpty()) {
            resultStatus.setCode("C011");
            resultStatus.setMessage("查無模板細節");
        } else {
            final TemplateDetail templateDetail = opt.get();
//            final TemplateDetailResponseDTO dto = constructTemplateDetailResponseDTO(templateDetail);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(templateDetail);
        }
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/prepareToPushMulti")
    public ResponseEntity<?> prepareToPushMulti(LineMultiRequestDTO lineMultiRequestDTO) {
        logger.info("[prepareToPushMulti]");
        ResultStatus resultStatus = new ResultStatus();
        Map<String, Object> textImageTemplate = flexMessageUtil.assembleTextImageTemplate(lineMultiRequestDTO.getTemplateDetailResponseDTO());
        Map<String, Object> data = new HashMap<>();
        data.put("to", lineMultiRequestDTO.getUserIds());
        data.put("messages", List.of(textImageTemplate));
        final ForestResponse<Void> pushMultiResponse = lineApi.pushMulti(configProperties.getChannelAccessToken(), data);
        if (pushMultiResponse.isSuccess()) {
            final TemplateSubject templateSubject = templateSubjectService.findById(lineMultiRequestDTO.getTemplateSubjectId()).get();
            templateSubject.setSendDate(sdf.format(new Date()));
            templateSubjectService.save(templateSubject);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        } else {
            resultStatus.setCode("C999");
            resultStatus.setMessage("例外發生");
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 用id查找模板的userIdList
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/findTemplateSubjectById/{id}")
    public ResponseEntity<?> findTemplateSubjectById (@PathVariable String id, HttpSession session){
        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[findTemplateSubjectById]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final Optional<TemplateSubject> subjectOpt = templateSubjectService.findById(id);
        if (subjectOpt.isEmpty()) {
            resultStatus.setCode("C010");
            resultStatus.setMessage("查無模板");
        } else {
            final TemplateSubject templateSubject = subjectOpt.get();
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(templateSubject.getUserIds()); //傳送userIds
        }
        return ResponseEntity.ok(resultStatus);
    }


    @NotNull
    private static TemplateSubjectResponseDTO constructTemplateSubjectResponseDTO(TemplateSubject templateSubject) {
        TemplateSubjectResponseDTO dto = new TemplateSubjectResponseDTO();
        dto.setId(templateSubject.getId());
        dto.setCreationDate(templateSubject.getCreationDate());
        dto.setTitle(templateSubject.getTitle());
        dto.setTemplateType(templateSubject.getTemplateType());
        dto.setDetailIds(templateSubject.getDetailIds());
        dto.setAltText(templateSubject.getAltText());
        dto.setSendDate(templateSubject.getSendDate());
        dto.setUserIds(templateSubject.getUserIds());
        return dto;
    }

    @NotNull
    private static TemplateDetailResponseDTO constructTemplateDetailResponseDTO(TemplateDetail templateDetail) {
        TemplateDetailResponseDTO dto = new TemplateDetailResponseDTO();
        dto.setId(templateDetail.getId());
        dto.setCreationDate(templateDetail.getCreationDate());
        dto.setButtonUrl(templateDetail.getButtonUrl());
        dto.setSubTitle(templateDetail.getSubTitle());
        dto.setButtonName(templateDetail.getButtonName());
        dto.setCover(templateDetail.getCover());
        dto.setMainTitle(templateDetail.getMainTitle());
        dto.setTextContent(templateDetail.getTextContent());
        return dto;
    }

    @NotNull
    private static FollowerResponseDTO constructFollowerResponseDTO(Follower follower) {
        FollowerResponseDTO dto = new FollowerResponseDTO();
        dto.setId(follower.getId());
        dto.setChannelID(follower.getChannelID());
        dto.setPictureUrl(follower.getPictureUrl());
        dto.setUserId(follower.getUserId());
        dto.setDisplayName(follower.getDisplayName());
        dto.setTag(follower.getTag());
        dto.setUnfollowTime(follower.getUnfollowTime());
        dto.setFollowTime(follower.getFollowTime());
        return dto;
    }
}

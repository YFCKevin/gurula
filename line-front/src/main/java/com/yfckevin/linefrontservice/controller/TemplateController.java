package com.yfckevin.linefrontservice.controller;

import com.dtflys.forest.http.ForestResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yfckevin.api.api.line.TemplateApi;
import com.yfckevin.common.dto.line.*;
import com.yfckevin.common.enums.TemplateType;
import com.yfckevin.common.exception.ResultStatus;
import com.yfckevin.linefrontservice.ConfigProperties;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TemplateController {
    Logger logger = LoggerFactory.getLogger(TemplateController.class);
    private final ConfigProperties configProperties;
    private final TemplateApi templateApi;
    private final SimpleDateFormat sdf;

    public TemplateController(ConfigProperties configProperties, TemplateApi templateApi, @Qualifier("sdf") SimpleDateFormat sdf) {
        this.configProperties = configProperties;
        this.templateApi = templateApi;
        this.sdf = sdf;
    }

    /**
     * 導頁到模板管理介面
     *
     * @param session
     * @param model
     * @return
     */
    @GetMapping("/forwardTemplateManagement")
    public String forwardTemplateManagement(HttpSession session, Model model) {
        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[forwardTemplateManagement]");
        }
//        else {
//            return "redirect:" + configProperties.getBackendLoginDomain() + "backendLogin";
//        }
        List<TemplateDTO> templateDTOList = new ArrayList<>();
        final ForestResponse<ResultStatus<List<TemplateSubjectResponseDTO>>> templateSubjectList = templateApi.getAllTemplate();
        templateDTOList = templateSubjectList.getResult().getData().stream()
                .map(this::constructTemplateDTO)
                .collect(Collectors.toList());

        model.addAttribute("templateDTOList", templateDTOList);

        return "templateManagement";
    }


    /**
     * 列出全部追蹤者
     *
     * @param session
     * @return
     */
    @GetMapping("/showFollowers")
    public ResponseEntity<?> showFollowers(HttpSession session) {
        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[showFollowers]");
        }
        ResultStatus resultStatus = new ResultStatus();
        ForestResponse<ResultStatus<List<FollowerResponseDTO>>> followerList = templateApi.findAllFollower();
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(followerList.getResult().getData());
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 新增模板
     * @param dto
     * @param session
     * @return
     */
    @PostMapping("/addTemplateSubject")
    public ResponseEntity<?> addTemplateSubject(@RequestBody TemplateSubjectDTO dto, HttpSession session) {
        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[addTemplateSubject]");
        }
        ResultStatus resultStatus = new ResultStatus();

        if (StringUtils.isBlank(dto.getId())) { //新增
            TemplateSubjectRequestDTO subjectRequestDTO = new TemplateSubjectRequestDTO();
            subjectRequestDTO.setTitle(dto.getTitle());
            subjectRequestDTO.setAltText(dto.getAltText());
            subjectRequestDTO.setCreationDate(sdf.format(new Date()));
            subjectRequestDTO.setUserIds(dto.getUserIds());
            subjectRequestDTO.setTemplateType(TemplateType.valueOf(dto.getTemplateType()));
            ForestResponse<ResultStatus<TemplateSubjectResponseDTO>> savedTemplateSubject = templateApi.saveTemplateSubject(subjectRequestDTO);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(savedTemplateSubject.getResult().getData());
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 更換發送的追蹤者
     * @param dto
     * @param session
     * @return
     */
    @PostMapping("/changeSendPeople")
    public ResponseEntity<?> changeSendPeople (@RequestBody PushRequestDTO dto, HttpSession session){
        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[changeSendPeople]");
        }
        ResultStatus resultStatus = new ResultStatus();
        ForestResponse<ResultStatus<TemplateSubjectResponseDTO>> oneTemplateSubjectInfo = templateApi.oneTemplateSubjectInfo(dto.getSubjectId());
        if ("C010".equals(oneTemplateSubjectInfo.getResult().getCode())) {
            resultStatus.setCode("C010");
            resultStatus.setMessage("查無模板");
        } else {
            final TemplateSubjectResponseDTO templateSubject = oneTemplateSubjectInfo.getResult().getData();
            templateSubject.setUserIds(dto.getUserIdList());
            TemplateSubjectRequestDTO subjectRequestDTO = constructTemplateSubjectRequestDTO(templateSubject);
            templateApi.saveTemplateSubject(subjectRequestDTO);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 追蹤者模糊查詢
     * @param searchDTO
     * @param session
     * @return
     */
    @PostMapping("/searchFollower")
    public ResponseEntity<?> searchFollower (@RequestBody SearchDTO searchDTO, HttpSession session){
        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[searchFollower]");
        }
        ResultStatus resultStatus = new ResultStatus();

        ForestResponse<ResultStatus<List<FollowerResponseDTO>>> followerList = templateApi.searchFollower(searchDTO);
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(followerList.getResult().getData());

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 模板模糊查詢
     * @param searchDTO
     * @param session
     * @return
     */
    @PostMapping("/templateSearch")
    public ResponseEntity<?> templateSearch (@RequestBody SearchDTO searchDTO, HttpSession session){
        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[templateSearch]");
        }
        ResultStatus resultStatus = new ResultStatus();

        ForestResponse<ResultStatus<List<TemplateSubjectResponseDTO>>> subjectList = templateApi.templateSearch(searchDTO);
        List<TemplateDTO> templateDTOList = subjectList.getResult().getData().stream()
                .map(this::constructTemplateDTO)
                .collect(Collectors.toList());

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(templateDTOList);

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 新增模板細節
     * @param dto
     * @param session
     * @return
     * @throws IOException
     */
    @PostMapping("/addTemplateDetail")
    public String addTemplateDetail(@ModelAttribute TemplateDetailDTO dto, HttpSession session) throws IOException {
        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[addTemplateDetail]");
        }

        final ForestResponse<ResultStatus<?>> response = templateApi.addTemplateDetail(
                dto.getMultipartFile(),
                dto.getId(),
                dto.getSubjectId(),
                dto.getMainTitle(),
                dto.getSubTitle(),
                dto.getTextContent(),
                dto.getCoverPath(),
                dto.getCreationDate(),
                dto.getButtonName(),
                dto.getButtonUrl()
        );

        final String responseCode = response.getResult().getCode();
        if ("C000".equals(responseCode)) {
            return "redirect:" + configProperties.getGlobalDomain() + "forwardTemplateManagement";
        } else {
            return "redirect:/error/50x";
        }
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

        final ForestResponse<ResultStatus<List<String>>> response = templateApi.getSelectedUserId(id);
        final String responseCode = response.getResult().getCode();
        final String message = response.getResult().getMessage();
        final List<String> selectedUserId = response.getResult().getData();
        if ("C000".equals(responseCode)) {
            resultStatus.setCode(responseCode);
            resultStatus.setMessage(message);
            resultStatus.setData(selectedUserId);
        } else {
            resultStatus.setCode(responseCode);
            resultStatus.setMessage(message);
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 後台推播功能 (多人推播)
     * @param dto
     * @param session
     * @return
     * @throws JsonProcessingException
     */
    @PostMapping("/pushSelectedFollowers")
    public ResponseEntity<?> pushSelectedFollowers(@RequestBody PushRequestDTO dto, HttpSession session) throws JsonProcessingException {
        final String member = (String) session.getAttribute("admin");
        if (member != null) {
            logger.info("[pushSelectedFollowers]");
        }
        ResultStatus resultStatus = new ResultStatus();

        try {
            final ForestResponse<ResultStatus<TemplateSubjectResponseDTO>> oneTemplateSubjectInfo = templateApi.oneTemplateSubjectInfo(dto.getSubjectId());
            final ResultStatus<TemplateSubjectResponseDTO> subjectResult = oneTemplateSubjectInfo.getResult();
            if ("C010".equals(subjectResult.getCode())) {
                resultStatus.setCode("C010");
                resultStatus.setMessage("查無模板");
                return ResponseEntity.ok(resultStatus);
            } else {
                final TemplateSubjectResponseDTO templateSubject = subjectResult.getData();
                if (templateSubject.getUserIds().size() == 0){
                    resultStatus.setCode("C012");
                    resultStatus.setMessage("推播人數不得為0");
                    return ResponseEntity.ok(resultStatus);
                }
                final ForestResponse<ResultStatus<TemplateDetailResponseDTO>> oneTemplateDetailInfo = templateApi.oneTemplateDetailInfo(templateSubject.getDetailIds().get(0));
                final ResultStatus<TemplateDetailResponseDTO> detailResult = oneTemplateDetailInfo.getResult();
                if ("C011".equals(detailResult.getCode())) {
                    resultStatus.setCode("C011");
                    resultStatus.setMessage("查無模板細節");
                    return ResponseEntity.ok(resultStatus);
                } else {
                    final TemplateDetailResponseDTO templateDetail = detailResult.getData();

                    //組建傳給line的template
                    LineMultiRequestDTO lineMultiRequestDTO = new LineMultiRequestDTO();
                    lineMultiRequestDTO.setUserIds(templateSubject.getUserIds());
                    lineMultiRequestDTO.setTemplateDetailResponseDTO(templateDetail);
                    lineMultiRequestDTO.setTemplateSubjectId(templateSubject.getId());
                    final ForestResponse<ResultStatus<TemplateSubjectResponseDTO>> templateSubjectResponseDTO = templateApi.prepareToPushMulti(lineMultiRequestDTO);
                    if ("C000".equals(templateSubjectResponseDTO.getResult().getCode())) {
                        resultStatus.setCode("C000");
                        resultStatus.setMessage("成功");
                        return ResponseEntity.ok(resultStatus);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return ResponseEntity.ok(resultStatus);
    }


    @ModelAttribute
    public void setGlobalDomain(Model model) {
        model.addAttribute("badmintonFrontDomain", configProperties.getBadmintonFrontDomain());
    }

    @NotNull
    private static TemplateSubjectRequestDTO constructTemplateSubjectRequestDTO(TemplateSubjectResponseDTO templateSubject) {
        TemplateSubjectRequestDTO subjectRequestDTO = new TemplateSubjectRequestDTO();
        subjectRequestDTO.setId(templateSubject.getId());
        subjectRequestDTO.setTemplateType(templateSubject.getTemplateType());
        subjectRequestDTO.setSendDate(templateSubject.getSendDate());
        subjectRequestDTO.setTitle(templateSubject.getTitle());
        subjectRequestDTO.setAltText(templateSubject.getAltText());
        subjectRequestDTO.setDetailIds(templateSubject.getDetailIds());
        subjectRequestDTO.setUserIds(templateSubject.getUserIds());
        subjectRequestDTO.setCreationDate(templateSubject.getCreationDate());
        return subjectRequestDTO;
    }

    private TemplateDTO constructTemplateDTO(TemplateSubjectResponseDTO s) {
        TemplateDTO dto = new TemplateDTO();
        dto.setSubjectId(s.getId());
        dto.setSubjectTitle(s.getTitle());
        dto.setSubjectAltText(s.getAltText());
        dto.setCreationDate(s.getCreationDate());
        dto.setTemplateType(s.getTemplateType().getLabel());

        //組模板細節
        ForestResponse<ResultStatus<List<TemplateDetailResponseDTO>>> templateDetailList = templateApi.getTemplateDetailByIdIn(s.getDetailIds());
        List<TemplateDetailDTO> detailDTOList = templateDetailList.getResult().getData().stream().map(d -> {
            TemplateDetailDTO detailDTO = new TemplateDetailDTO();
            detailDTO.setId(d.getId());
            detailDTO.setMainTitle(d.getMainTitle());
            detailDTO.setSubTitle(d.getSubTitle());
            detailDTO.setTextContent(d.getTextContent());
            detailDTO.setCoverPath(d.getCover());
            detailDTO.setCreationDate(d.getCreationDate());
            detailDTO.setButtonName(d.getButtonName());
            detailDTO.setButtonUrl(d.getButtonUrl());
            return detailDTO;
        }).collect(Collectors.toList());

        dto.setDetailDTOList(detailDTOList);

        if (s.getUserIds().size() > 0) {
            dto.setUserIds(s.getUserIds());
        }

        return dto;
    }
}

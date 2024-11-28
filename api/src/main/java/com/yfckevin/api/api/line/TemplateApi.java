package com.yfckevin.api.api.line;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.common.dto.line.*;
import com.yfckevin.common.exception.ResultStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Component
@BaseRequest(baseURL = "${lineServiceDomain}")
public interface TemplateApi {
    @Get(url = "/getAllTemplate")
    ForestResponse<ResultStatus<List<TemplateSubjectResponseDTO>>> getAllTemplate();

    @Post(url = "/getTemplateDetailByIdIn", contentType = "application/json")
    ForestResponse<ResultStatus<List<TemplateDetailResponseDTO>>> getTemplateDetailByIdIn(@Body List<String> detailIds);

    @Get(url = "/findAllFollower")
    ForestResponse<ResultStatus<List<FollowerResponseDTO>>> findAllFollower();

    @Post(url = "/saveTemplateSubject", contentType = "application/json")
    ForestResponse<ResultStatus<TemplateSubjectResponseDTO>> saveTemplateSubject(@Body TemplateSubjectRequestDTO subjectRequestDTO);

    @Get(url = "/oneTemplateSubjectInfo/{subjectId}")
    ForestResponse<ResultStatus<TemplateSubjectResponseDTO>> oneTemplateSubjectInfo(@Var("subjectId") String subjectId);

    @Post(url = "/searchFollower", contentType = "application/json")
    ForestResponse<ResultStatus<List<FollowerResponseDTO>>> searchFollower(@Body SearchDTO searchDTO);

    @Post(url = "/templateSearch", contentType = "application/json")
    ForestResponse<ResultStatus<List<TemplateSubjectResponseDTO>>> templateSearch(@Body SearchDTO searchDTO);

    @Redirection
    @Post(url = "/addTemplateDetail")
    ForestResponse<ResultStatus<?>> addTemplateDetail(
            @DataFile("multipartFile") MultipartFile file,
            @Body("id") String id,
            @Body("subjectId") String subjectId,
            @Body("mainTitle") String mainTitle,
            @Body("subTitle") String subTitle,
            @Body("textContent") String textContent,
            @Body("coverPath") String coverPath,
            @Body("creationDate") String creationDate,
            @Body("buttonName") String buttonName,
            @Body("buttonUrl") String buttonUrl
    );

    @Get(url = "/oneTemplateDetailInfo/{detailId}")
    ForestResponse<ResultStatus<TemplateDetailResponseDTO>> oneTemplateDetailInfo(@Var("detailId") String detailId);

    @Post(url = "/prepareToPushMulti", contentType = "application/json")
    ForestResponse<ResultStatus<TemplateSubjectResponseDTO>> prepareToPushMulti(@Body LineMultiRequestDTO lineMultiRequestDTO);

    @Get(url = "/findTemplateSubjectById/{templateSubjectId}")
    ForestResponse<ResultStatus<List<String>>> getSelectedUserId(@Var("templateSubjectId") String templateSubjectId);
}

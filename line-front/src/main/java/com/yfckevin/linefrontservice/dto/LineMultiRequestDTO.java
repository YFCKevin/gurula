package com.yfckevin.linefrontservice.dto;

import java.util.ArrayList;
import java.util.List;

public class LineMultiRequestDTO {
    private List<String> userIds = new ArrayList<>();   //傳送對象
    private String templateSubjectId;
    private TemplateDetailResponseDTO templateDetailResponseDTO;

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public TemplateDetailResponseDTO getTemplateDetailResponseDTO() {
        return templateDetailResponseDTO;
    }

    public void setTemplateDetailResponseDTO(TemplateDetailResponseDTO templateDetailResponseDTO) {
        this.templateDetailResponseDTO = templateDetailResponseDTO;
    }

    public String getTemplateSubjectId() {
        return templateSubjectId;
    }

    public void setTemplateSubjectId(String templateSubjectId) {
        this.templateSubjectId = templateSubjectId;
    }
}

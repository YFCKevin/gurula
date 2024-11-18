package com.yfckevin.common.dto.inkCloud;

import com.yfckevin.common.enums.VisionType;

public class SearchDTO {
    private String keyword;
    private String startDate;
    private String endDate;
    private String memberId;
    private String visionType;

    public String getVisionType() {
        return visionType;
    }

    public void setVisionType(String visionType) {
        this.visionType = visionType;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "SearchDTO{" +
                "keyword='" + keyword + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", memberId='" + memberId + '\'' +
                ", visionType='" + visionType + '\'' +
                '}';
    }
}


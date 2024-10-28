package com.yfckevin.api.badminton.dto;

import java.util.Set;

public class LeaderUserIdListDTO {
    private Set<String> userIdList;

    public Set<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(Set<String> userIdList) {
        this.userIdList = userIdList;
    }
}

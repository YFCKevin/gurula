package com.yfckevin.api.api.member;

import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.common.dto.member.MemberDTO;
import com.yfckevin.common.exception.ResultStatus;
import org.springframework.stereotype.Component;

@Component
@BaseRequest(baseURL = "${memberDomain}")
public interface MemberApi {

    @Get(url = "/getMemberInfo/{memberId}")
    ForestResponse<ResultStatus<MemberDTO>> getMemberInfo(@Var("memberId") String memberId);

}

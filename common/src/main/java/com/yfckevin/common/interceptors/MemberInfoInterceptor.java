package com.yfckevin.common.interceptors;


import com.yfckevin.common.utils.MemberContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MemberInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.拿到會員資料
        final String memberId = request.getHeader("member-info");

        //2.判斷是否拿到，如果有則存入ThreadLocal
        if (StringUtils.isNotBlank(memberId)) {
            MemberContext.setMember(memberId);
        }


        //3.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清空會員資料
        MemberContext.removeMember();
    }
}

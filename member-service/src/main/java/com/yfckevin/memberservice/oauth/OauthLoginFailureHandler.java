package com.yfckevin.memberservice.oauth;

import com.yfckevin.memberservice.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *  第三方登入失敗或取消會進來處理
 */
@Component
public class OauthLoginFailureHandler implements AuthenticationFailureHandler {
    protected Logger logger = LoggerFactory.getLogger(OauthLoginFailureHandler.class);
    private final ConfigProperties configProperties;

    public OauthLoginFailureHandler(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String serviceName = (String) request.getSession().getAttribute("service");
        System.out.println("serviceName = " + serviceName);
        switch (serviceName) {
            case "badminton":
                response.sendRedirect(configProperties.getBadmintonDomain() + "index");
                break;
            case "inkCloud":
                response.sendRedirect(configProperties.getInkCloudDomain() + "login.html");
                break;
            case "bingBao":
                response.sendRedirect(configProperties.getBingBaoDomain() + "login.html");
                break;
        }
    }

}

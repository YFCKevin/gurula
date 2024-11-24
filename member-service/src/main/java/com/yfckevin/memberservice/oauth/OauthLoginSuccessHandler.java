package com.yfckevin.memberservice.oauth;

import com.yfckevin.memberservice.ConfigProperties;
import com.yfckevin.memberservice.entity.Member;
import com.yfckevin.memberservice.utils.JwtTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

@Component
public class OauthLoginSuccessHandler implements AuthenticationSuccessHandler {
    protected Logger logger = LoggerFactory.getLogger(OauthLoginSuccessHandler.class);
    private final UserService userService;
    private final ConfigProperties configProperties;
    private final JwtTool jwtTool;

    public OauthLoginSuccessHandler(UserService userService, ConfigProperties configProperties, JwtTool jwtTool) {
        this.userService = userService;
        this.configProperties = configProperties;
        this.jwtTool = jwtTool;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        System.out.println("第三方登入成功後要做的");

        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        System.out.println(oauthUser.getOauth2ClientName());

        //處理把第三方的帳號儲存到DB，Oauth2ClientName是GOOGLE、FACEBOOK、LINE.....
        Member member  = userService.processOAuthPostLogin(oauthUser.getEmail(),oauthUser.getName(),oauthUser.getOauth2ClientName());
        //產生jwt token並回傳到前端
        String token = jwtTool.createToken(member.getId(), String.valueOf(member.getRole()), Duration.ofMinutes(30));
        System.out.println("token = " + token);

        // 設定 Cookie
        ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", token)
                .secure(true)
                .path("/")
                .maxAge(20 * 60)
                .sameSite("Strict")
                .build();

        response.setHeader("Set-Cookie", cookie.toString());

        String serviceName = (String) request.getSession().getAttribute("service");
        System.out.println("serviceName = " + serviceName);
        switch (serviceName) {
            case "badminton":
                response.sendRedirect(configProperties.getBadmintonDomain() + "index");
                break;
            case "inkCloud":
                response.sendRedirect(configProperties.getInkCloudDomain() + "index.html");
                break;
            case "bingBao":
                response.sendRedirect(configProperties.getBingBaoDomain() + "dashboard.html");
                break;
        }
    }

}

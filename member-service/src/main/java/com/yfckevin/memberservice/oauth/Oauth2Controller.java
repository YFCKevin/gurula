package com.yfckevin.memberservice.oauth;

import com.yfckevin.memberservice.ConfigProperties;
import com.yfckevin.memberservice.config.LineProperties;
import com.yfckevin.memberservice.config.LineProviderProperties;
import com.yfckevin.memberservice.entity.Member;
import com.yfckevin.memberservice.enums.Role;
import com.yfckevin.memberservice.service.MemberService;
import com.yfckevin.memberservice.utils.JwtTool;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Controller
public class Oauth2Controller {
    private final ConfigProperties configProperties;
    private final LineProperties lineProperties;
    private final LineProviderProperties lineProviderProperties;
    private final MemberService memberService;
    private final RestTemplate restTemplate;
    private final JwtTool jwtTool;

    public Oauth2Controller(ConfigProperties configProperties, LineProperties lineProperties, LineProviderProperties lineProviderProperties, MemberService memberService, RestTemplate restTemplate, JwtTool jwtTool) {
        this.configProperties = configProperties;
        this.lineProperties = lineProperties;
        this.lineProviderProperties = lineProviderProperties;
        this.memberService = memberService;
        this.restTemplate = restTemplate;
        this.jwtTool = jwtTool;
    }


    @GetMapping("/callback")
    public String handleOAuth2Callback(@RequestParam("code") String code, HttpServletResponse response) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = "grant_type=authorization_code" +
                "&code=" + code +
                "&redirect_uri=" + configProperties.getGlobalDomain() + "callback" +
                "&client_id=" + lineProperties.getClientId() +
                "&client_secret=" + lineProperties.getClientSecret();

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                lineProviderProperties.getTokenUri(),
                HttpMethod.POST,
                requestEntity,
                Map.class);

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userRequestEntity = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                lineProviderProperties.getUserInfoUri(),
                HttpMethod.GET,
                userRequestEntity,
                Map.class);

        Map<String, Object> userInfo = userResponse.getBody();

        String userId = (String) userInfo.get("userId");
        String userName = (String) userInfo.get("displayName");
        final String pictureUrl = (String) userInfo.get("pictureUrl");

        Optional<Member> memberOpt = memberService.findByUserId(userId);
        Member member;
        if (memberOpt.isEmpty()) {
            member = new Member();
            member.setName(userName);
            member.setPictureUrl(pictureUrl);
            member.setUserId(userId);
            member.setRole(Role.USER);
            member = memberService.save(member);
        } else {
            member = memberOpt.get();
        }

        //產生jwt token並回傳到前端
        String token = jwtTool.createToken(member.getId(), String.valueOf(member.getRole()), Duration.ofMinutes(30));
        System.out.println("token = " + token);

        // 設定 Cookie
        ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", token)
//                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(20 * 60)
                .sameSite("Strict")
                .build();

        response.setHeader("Set-Cookie", cookie.toString());

        return "redirect:" + configProperties.getGlobalDomain() + "index.html";
    }
}

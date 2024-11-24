package com.yfckevin.memberservice.controller;

import com.yfckevin.memberservice.ConfigProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class LoginController {

    private final ConfigProperties configProperties;

    public LoginController(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    /**
     * 登入導向的先行判定，並放入到session
     * @param type google or line
     * @param service 服務名稱
     * @param request
     * @return
     */
    @GetMapping("/login")
    public String login(@RequestParam("type") String type, @RequestParam("service") String service, HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("service = " + service);
        // 將 serviceName 存入 session
        HttpSession session = request.getSession();
        session.setAttribute("service", service);

        switch (type) {
            case "google":
                System.out.println(123);
                response.sendRedirect(configProperties.getGlobalDomain() + "oauth2/authorization/google");
                break;
            case "line":
                System.out.println(456);
                response.sendRedirect(configProperties.getGlobalDomain() + "oauth2/authorization/line");
                break;
        }

        return "";
    }
}

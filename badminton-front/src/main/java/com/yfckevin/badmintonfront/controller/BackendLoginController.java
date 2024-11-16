package com.yfckevin.badmintonfront.controller;

import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.badminton.api.badminton.LoginApi;
import com.yfckevin.api.badminton.dto.badminton.LoginDTO;
import com.yfckevin.common.exception.ResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class BackendLoginController {
    Logger logger = LoggerFactory.getLogger(BackendLoginController.class);
    private final LoginApi loginApi;

    public BackendLoginController(LoginApi loginApi) {
        this.loginApi = loginApi;
    }

    /**
     * 導登入頁面
     *
     * @return
     */
    @GetMapping("/backendLogin")
    public String backendLogin() {
        logger.info("[backendLogin]");
        return "backend/login";
    }


    /**
     * 登入驗證
     *
     * @return
     */
    @PostMapping("/loginCheck")
    public ResponseEntity<?> loginCheck(@RequestBody LoginDTO dto, HttpSession session) throws IOException {
        logger.info("[loginCheck]");
        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<?>> loginCheck = loginApi.loginCheck(dto);
        if ("C000".equals(loginCheck.getResult().getCode())) {
            session.setAttribute("admin", dto.getAccount());
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        } else {
            resultStatus.setCode("C001");
            resultStatus.setMessage("帳號或密碼錯誤");
        }
        return ResponseEntity.ok(resultStatus);
    }

    /**
     * 登出
     *
     * @param session
     * @return
     */
    @GetMapping("/backendLogout")
    public String backendLogout(HttpSession session) {
        logger.info("[backendLogout]");
        session.removeAttribute("admin");
        return "redirect:/backendLogin";
    }




}

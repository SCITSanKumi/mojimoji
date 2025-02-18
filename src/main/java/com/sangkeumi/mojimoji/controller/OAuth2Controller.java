package com.sangkeumi.mojimoji.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OAuth2Controller {

    @GetMapping("/oauth2/success")
    public String oauthSuccessPage() {
        // Thymeleaf 템플릿 "oauth2Success.html" 반환
        return "/user/oauth2Success";
    }

}

package com.sangkeumi.mojimoji.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyPageContoller {

    @GetMapping("/mypage")
    public String mypage() {

        return "mypage";
    }
}

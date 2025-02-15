package com.sangkeumi.mojimoji.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyPageContoller {

    @GetMapping("/myPage")
    public String myPage() {

        return "myPage";
    }
}

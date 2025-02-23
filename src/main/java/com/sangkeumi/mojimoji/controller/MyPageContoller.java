package com.sangkeumi.mojimoji.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sangkeumi.mojimoji.dto.user.MyPrincipal;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class MyPageContoller {

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal MyPrincipal myPrincipalm, Model model) {

        return "/mypage";
    }
}

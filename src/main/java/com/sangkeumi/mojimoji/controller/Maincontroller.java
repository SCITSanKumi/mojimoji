package com.sangkeumi.mojimoji.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sangkeumi.mojimoji.dto.user.MyPrincipal;

@Controller
public class Maincontroller {

    @GetMapping("/")
    public String main(@AuthenticationPrincipal MyPrincipal principal, Model model) {

        if (principal != null) {
            Long userId = principal.getUserId();
            String nickname = principal.getNickname();
            model.addAttribute("userId", userId);
            model.addAttribute("nickname", nickname);
        }
        return "/index";
    }
}

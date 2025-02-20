package com.sangkeumi.mojimoji.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sangkeumi.mojimoji.dto.user.CustomUser;
import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;

    @GetMapping("/")
    public String main(@AuthenticationPrincipal MyPrincipal principal, Model model) {

        if (principal != null && principal.getUserId() != null) {
            Long userId = principal.getUserId();

            // DTO 조회
            CustomUser customUser = userService.getUserDto(userId);

            model.addAttribute("userId", customUser.userId());
            model.addAttribute("nickname", customUser.nickname());
        }
        return "index";
    }
}

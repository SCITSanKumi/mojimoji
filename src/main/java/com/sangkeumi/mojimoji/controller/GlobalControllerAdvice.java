package com.sangkeumi.mojimoji.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.service.UserService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    private final UserService userService;

    @ModelAttribute("userProfileUrl")
    public String getCurrentUser(@AuthenticationPrincipal MyPrincipal principal) {
        if (principal == null) {
            return null;
        }

        return userService.getUserProfileImageUrl(principal.getUserId());
    }
}

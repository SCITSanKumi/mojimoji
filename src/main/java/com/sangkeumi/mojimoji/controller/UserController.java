package com.sangkeumi.mojimoji.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sangkeumi.mojimoji.dto.user.*;
import com.sangkeumi.mojimoji.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "User API", description = "유저 관련 API")
public class UserController {
    
    private final UserService userService;

    // 로그인 페이지 이동
    @GetMapping("/sign-in")
    public String signIn() {
        return "/user/signIn";
    }

    @GetMapping("/sign-up")
    public String signUp() {
        return "/user/signUp";
    }

    @PostMapping("/sign-up")
    public String signUp(@ModelAttribute UserSignup userSignup) {
        boolean result = userService.signUp(userSignup);
        // 회원가입 성공하면 메인페이지로 리다이렉트
        if (result) {
            return "redirect:/";
            // 회원가입 실패하면 다시 회원가입 페이지로
        } else {
            return "redirect:/user/sign-up";
        }
    }

    @ResponseBody
    @PostMapping("/id-check")
    @Operation(summary = "id-check API", description = "아이디 중복 체크")
    public boolean idCheck(@RequestBody IdCheck idCheck) {
        boolean result = userService.existByUsername(idCheck.username());
        return result; // true 면 중복아님 사용가능 , false 면 중복 사용 불가
    }
}

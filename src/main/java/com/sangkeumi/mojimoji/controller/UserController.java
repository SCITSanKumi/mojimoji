package com.sangkeumi.mojimoji.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    @GetMapping("/login")
    public String login() {
        return "/user/signIn";
    }

    @GetMapping("/sign-up")
    public String regist() {
        return "/user/signUp";
    }

    @PostMapping("/sign-up")
    @ResponseBody
    @Operation(summary = "회원가입", description = "회원가입을 진행한다.")
    public boolean signUp(@RequestBody UserSignup userSignup) {
        return userService.signUp(userSignup); // true 면 회원가입 성공, false 면 회원가입 실패
    }

    @GetMapping("/nickname-check")
    @ResponseBody
    @Operation(summary = "닉네임 중복 체크", description = "닉네임이 이미 사용중인지 확인한다.")
    public boolean nicknameCheck(@RequestParam(name = "nickname") String nickname) {
        return userService.existByNickname(nickname); // true 면 중복아님 사용가능 , false 면 중복 사용 불가
    }

    @GetMapping("/id-check")
    @ResponseBody
    @Operation(summary = "아이디 중복 체크", description = "아이디가 이미 사용중인지 확인한다.")
    public boolean idCheck(@RequestParam(name = "username") String username) {
        return userService.existByUsername(username); // true 면 중복아님 사용가능 , false 면 중복 사용 불가
    }

    @GetMapping("/email-check")
    @ResponseBody
    @Operation(summary = "이메일 중복 체크", description = "이메일이 이미 사용중인지 확인한다.")
    public boolean emailCheck(@RequestParam(name = "email") String email) {
        return userService.existByEmail(email); // true 면 중복아님 사용가능 , false 면 중복 사용 불가
    }
}

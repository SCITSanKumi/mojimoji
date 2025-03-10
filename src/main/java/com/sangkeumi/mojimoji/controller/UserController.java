package com.sangkeumi.mojimoji.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.sangkeumi.mojimoji.dto.board.OtherProfileResponse;
import com.sangkeumi.mojimoji.dto.board.OtherStoryListResponse;
import com.sangkeumi.mojimoji.dto.user.*;
import com.sangkeumi.mojimoji.service.BoardService;
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
    private final BoardService boardService;

    // 로그인 페이지 이동
    @GetMapping("/login")
    public String login() {
        return "user/signIn";
    }

    @GetMapping("/sign-up")
    public String regist() {
        return "user/signUp";
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

    /**
     * 회원 정보 수정 페이지를 반환하는 메서드
     * @param principal
     * @param model
     * @return
     */
    @GetMapping("/update")
    public String profileUpdate(@AuthenticationPrincipal MyPrincipal principal, Model model) {
        Long userId = principal.getUserId();
        UserUpdateView user = userService.findById(userId);
        model.addAttribute("user", user);
        return "user/profileUpdate";
    }

    /**
     * 닉네임 및 이메일 수정
     * @param principal
     * @param updateRequest
     * @return
     */
    @PostMapping("/update")
    @ResponseBody
    @Operation(summary = "회원 정보 수정", description = "닉네임과 이메일을 수정한다.")
    public boolean updateProfile(@AuthenticationPrincipal MyPrincipal principal,
            @RequestBody UserUpdateRequest updateRequest) {
        Long userId = principal.getUserId();
        return userService.updateProfile(userId, updateRequest.nickname(), updateRequest.email());
    }
    
    /**
     * 비밀번호 수정 (AJAX 요청)
     * @param principal
     * @param passwordRequest
     * @return
     */
    @PostMapping("/update/password")
    @ResponseBody
    @Operation(summary = "비밀번호 변경", description = "기존 비밀번호 확인 후 새 비밀번호로 변경한다.")
    public ResponseEntity<Boolean> updatePassword(
            @AuthenticationPrincipal com.sangkeumi.mojimoji.dto.user.MyPrincipal principal,
            @RequestBody PasswordUpdateRequest passwordRequest) {
        Long userId = principal.getUserId();
        boolean result = userService.updatePassword(userId, passwordRequest.currentPassword(),
                passwordRequest.newPassword());
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/delete")
    @ResponseBody
    @Operation(summary = "계정 삭제", description = "계정을 삭제한다.")
    public ResponseEntity<Boolean> deleteAccount(@AuthenticationPrincipal MyPrincipal principal) {
        Long userId = principal.getUserId();
        boolean result = userService.deleteUser(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/otherProfile")
    public String viewOtherProfile(@RequestParam(name = "userId") Long userId, Model model) {
        List<OtherStoryListResponse> storyList = boardService.getStoriesByUserId(userId, 0, 8);
        OtherProfileResponse otherProfile = boardService.getOtherProfile(userId);
        Long BookCount = boardService.getBooksCount(userId);
        model.addAttribute("storyList", storyList);
        model.addAttribute("otherProfile", otherProfile);
        model.addAttribute("BookCount", BookCount);
        model.addAttribute("userId", userId);
        return "/user/otherProfile";
    }
}

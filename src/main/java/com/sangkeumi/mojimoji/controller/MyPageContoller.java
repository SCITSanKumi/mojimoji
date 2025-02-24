package com.sangkeumi.mojimoji.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sangkeumi.mojimoji.dto.mypage.JlptCollectionStats;
import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.service.KanjiCollectionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MyPageContoller {

    private final KanjiCollectionService kanjiCollectionService;

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal MyPrincipal myPrincipal, Model model) {

        Long userId = myPrincipal.getUserId();
        List<JlptCollectionStats> stats = kanjiCollectionService.getJlptStats(userId);

        model.addAttribute("stats", stats);
        return "/mypage";
    }
}

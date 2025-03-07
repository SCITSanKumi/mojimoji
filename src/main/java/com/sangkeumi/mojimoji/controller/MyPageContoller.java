package com.sangkeumi.mojimoji.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sangkeumi.mojimoji.dto.mypage.*;
import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.service.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@Tag(name = "Mypage API", description = "유저 통계 관련 API")
public class MyPageContoller {

    private final KanjiCollectionService kanjiCollectionService;
    private final BoardService boardService;

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal MyPrincipal myPrincipal, Model model) {

        Long userId = myPrincipal.getUserId();

        Long BookCount = boardService.getBooksCount(userId);

        // 1) 등급별 수집 통계 / 전체 수집 통계
        List<JlptCollectionStats> stats = kanjiCollectionService.getJlptStats(userId);
        // 2) 날짜별 수집 통계 (DailyAcquisitionStats)
        List<DailyAcquisitionStats> dailyStats = kanjiCollectionService.getDailyStats(userId);
        // 3) 일 평균 수집개수
        Double dailyAvg = kanjiCollectionService.getDailyAverage(userId);
        if (dailyAvg == null) {
            dailyAvg = 0.0;
        }
        // 4) 전부 수집한 카테고리 / 카테고리 총 개수
        CategoryCollectionSummary summary = kanjiCollectionService.getCategoryCollectionSummary(userId);

        // 5) 카테고리별 한자 목록
        Map<String, List<CategoryKanjiRow>> catMap = kanjiCollectionService.getCategoryKanjiMap(userId);

        model.addAttribute("BookCount", BookCount);
        model.addAttribute("stats", stats);
        model.addAttribute("dailyStats", dailyStats);
        model.addAttribute("dailyAvg", dailyAvg);
        model.addAttribute("summary", summary);
        model.addAttribute("catMap", catMap);

        return "/mypage";
    }
}

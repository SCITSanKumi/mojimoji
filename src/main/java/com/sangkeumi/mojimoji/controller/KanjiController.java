package com.sangkeumi.mojimoji.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sangkeumi.mojimoji.dto.kanji.myCollectionRequest;
import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.entity.Kanji;

import com.sangkeumi.mojimoji.service.KanjiCollectionService;
import com.sangkeumi.mojimoji.service.KanjiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sangkeumi.mojimoji.dto.kanji.KanjiDetailResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping("/kanji")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Kanji API", description = "한자 관련 API")
public class KanjiController {

    private final KanjiCollectionService kanjiCollectionService;
    private final KanjiService kanjiService;

    @GetMapping("/collection")
    public String kanjiCollection(@AuthenticationPrincipal MyPrincipal principal,
            @RequestParam(name = "category", defaultValue = "") String category,
            @RequestParam(name = "jlptRank", defaultValue = "") String jlptRank,
            @RequestParam(name = "kanjiSearch", defaultValue = "") String kanjiSearch,
            @RequestParam(name = "kanjiSort", defaultValue = "한자번호순") String kanjiSort,
            @RequestParam(name = "sortDirection", defaultValue = "오름차순") String sortDirection, Model model) {
        if (principal != null && principal.getUserId() != null) {

            Long userId = principal.getUserId();
            Integer collected = 0;

            List<myCollectionRequest> myCollection = kanjiCollectionService.getMyCollection(userId, category, jlptRank,
                    kanjiSearch, kanjiSort, sortDirection);

            for (myCollectionRequest count : myCollection) {
                if (count.getIsCollected() == 1) {
                    collected++;
                }
            }

            model.addAttribute("myCollection", myCollection);
            model.addAttribute("category", category);
            model.addAttribute("jlptRank", jlptRank);
            model.addAttribute("kanjiSearch", kanjiSearch);
            model.addAttribute("kanjiSort", kanjiSort);
            model.addAttribute("sortDirection", sortDirection);
            model.addAttribute("collected", collected);
            return "kanji/kanjiCollection";
        }

        return "kanji/kanjiCollection";
    }

    @GetMapping("/detail")
    public String kanjiDetail(@RequestParam(name = "kanjiId") Long kanjiId, Model model) {
        Kanji kanji = kanjiService.getKanji(kanjiId);

        model.addAttribute("kanji", kanji);

        return "kanji/kanjiDetail";
    }

    @GetMapping("/details")
    @ResponseBody
    @Operation(summary = "한자 정보", description = "한자의 정보와 수집한 날자를 화면에 전송한다")
    public KanjiDetailResponse getKanjiDetails(
            @RequestParam(name = "kanjiId") Long kanjiId,
            @AuthenticationPrincipal MyPrincipal principal) {
        return kanjiService.getKanjiDetail(kanjiId, principal.getUserId());
    }
}

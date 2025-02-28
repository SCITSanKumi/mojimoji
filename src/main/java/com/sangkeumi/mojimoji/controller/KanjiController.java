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
import com.sangkeumi.mojimoji.dto.kanji.KanjiSearchRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
    public String kanjiCollection(
        @RequestBody KanjiSearchRequest searchRequest,
        @AuthenticationPrincipal MyPrincipal principal,
        Model model) {
            Integer collected = 0;

            List<myCollectionRequest> myCollection = kanjiCollectionService.getMyCollection(searchRequest, principal.getUserId());

            for (myCollectionRequest count : myCollection) {
                if (count.getIsCollected() == 1) {
                    collected++;
                }
            }

            model.addAttribute("myCollection", myCollection);
            model.addAttribute("category", searchRequest.category());
            model.addAttribute("jlptRank", searchRequest.jlptRank());
            model.addAttribute("kanjiSearch", searchRequest.kanjiSearch());
            model.addAttribute("kanjiSort", searchRequest.kanjiSort());
            model.addAttribute("sortDirection", searchRequest.sortDirection());
            model.addAttribute("collected", collected);

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

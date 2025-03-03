package com.sangkeumi.mojimoji.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sangkeumi.mojimoji.dto.kanji.*;
import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.entity.Kanji;

import com.sangkeumi.mojimoji.service.KanjiCollectionService;
import com.sangkeumi.mojimoji.service.KanjiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
            List<KanjiSearchResponse> myCollection = kanjiCollectionService.getMyCollection(searchRequest, principal.getUserId());
            
            long collected = 
                myCollection.stream()
                            .filter(kanji -> kanji.getCollectedCount() > 0)
                            .count();

            model.addAttribute("myCollection", myCollection);
            model.addAttribute("searchRequest", searchRequest);
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
    
    @ResponseBody
    @PostMapping("/addCollection")
    @Operation(summary = "한자컬렉션 추가", description = "해당 kanjiId에 해당하는 한자를 컬렉션에 추가한다")
    public void addCollection(
            @RequestParam("kanjiId") Long kanjiId,
            @AuthenticationPrincipal MyPrincipal principal) {
        kanjiCollectionService.addCollection(kanjiId, principal.getUserId());
    }
}

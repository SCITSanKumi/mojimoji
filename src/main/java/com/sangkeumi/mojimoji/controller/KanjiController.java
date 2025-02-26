package com.sangkeumi.mojimoji.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sangkeumi.mojimoji.dto.kanji.KanjiDetailResponse;
import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.service.KanjiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/kanji")
@RequiredArgsConstructor
@Tag(name = "Kanji API", description = "한자 관련 API")
public class KanjiController {

    private final KanjiService kanjiService;

    @GetMapping("/collection")
    public String kanjiCollection() {
        return "kanji/kanjiCollection";
    }

    @GetMapping("/detail")
    public String kanjiDetail() {
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

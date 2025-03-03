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

import com.sangkeumi.mojimoji.service.KanjiCollectionService;
import com.sangkeumi.mojimoji.service.KanjiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sangkeumi.mojimoji.dto.kanji.KanjiCount;
import com.sangkeumi.mojimoji.dto.kanji.KanjiDetailResponse;
import com.sangkeumi.mojimoji.dto.kanji.KanjiSearchRequest;

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
    public String kanjiCollection(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "jlptRank", required = false) String jlptRank,
            @RequestParam(name = "kanjiSearch", required = false) String kanjiSearch,
            @RequestParam(name = "kanjiSort", defaultValue = "한자번호순") String kanjiSort,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
            @AuthenticationPrincipal MyPrincipal principal,
            Model model) {
        // 1) page=1(첫 페이지)
        int page = 1;

        // 2) DTO 생성
        KanjiSearchRequest searchRequest = new KanjiSearchRequest(
                category,
                jlptRank,
                kanjiSearch,
                kanjiSort,
                sortDirection);

        // (A) 전체 결과 기준의 카운트 쿼리
        KanjiCount countDto = kanjiCollectionService.findTotalAndCollected(searchRequest, principal.getUserId());
        Long totalCount = (countDto.getTotalCount() != null) ? countDto.getTotalCount() : 0;
        Long collectedCount = (countDto.getCollectedCount() != null) ? countDto.getCollectedCount() : 0;

        // (B) 현재 페이지(10개) 목록
        List<myCollectionRequest> myCollection = kanjiCollectionService.getMyCollection(searchRequest,
                principal.getUserId(), page);

        // model
        model.addAttribute("myCollection", myCollection);
        model.addAttribute("category", category);
        model.addAttribute("jlptRank", jlptRank);
        model.addAttribute("kanjiSearch", kanjiSearch);
        model.addAttribute("kanjiSort", kanjiSort);
        model.addAttribute("sortDirection", sortDirection);

        // 여기서 "전체 결과" 기준으로 collectedCount / totalCount
        model.addAttribute("collected", collectedCount);
        model.addAttribute("totalCount", totalCount);

        return "kanji/kanjiCollection";
    }

    /**
     * 무한 스크롤로 page=2,3... 요청 시 AJAX로 partial HTML 반환
     */
    @GetMapping("/collectionAjax")
    @Operation(summary = "한자 목록 AJAX", description = "추가 10개 로딩")
    public String kanjiCollectionAjax(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "jlptRank", required = false) String jlptRank,
            @RequestParam(name = "kanjiSearch", required = false) String kanjiSearch,
            @RequestParam(name = "kanjiSort", defaultValue = "한자번호순") String kanjiSort,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @AuthenticationPrincipal MyPrincipal principal,
            Model model) {
        // 검색/정렬 DTO
        KanjiSearchRequest searchRequest = new KanjiSearchRequest(
                category, jlptRank, kanjiSearch, kanjiSort, sortDirection);

        // 다음 페이지 로드
        List<myCollectionRequest> nextPage = kanjiCollectionService.getMyCollection(
                searchRequest, principal.getUserId(), page);

        model.addAttribute("myCollection", nextPage);

        // Thymeleaf fragment만 반환
        return "kanji/kanjiCollectionFragment :: cardList";
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

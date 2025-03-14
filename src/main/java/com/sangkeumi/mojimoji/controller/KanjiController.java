package com.sangkeumi.mojimoji.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.dto.kanji.*;
import com.sangkeumi.mojimoji.service.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping("/kanji")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Kanji API", description = "한자 관련 API")
public class KanjiController {

  private final KanjiCollectionService kanjiCollectionService;

  @GetMapping("/collection")
  public String kanjiCollection(
      @AuthenticationPrincipal MyPrincipal principal,
      @ModelAttribute KanjiSearchRequest searchRequest,
      Model model) {
    int page = 1;

    Page<KanjiSearchResponse> searchResponse = kanjiCollectionService.getMyCollection(principal.getUserId(),
        searchRequest, page);

    List<WrongKanji> wrongKanji = kanjiCollectionService.getWrongKanji(principal.getUserId());

    KanjiCount countDto = kanjiCollectionService.findTotalAndCollected(searchRequest,
        principal.getUserId());

    model.addAttribute("searchRequest", searchRequest);
    model.addAttribute("searchResponse", searchResponse.getContent());
    model.addAttribute("wrongKanji", wrongKanji);
    // 여기서 "전체 결과" 기준으로 collectedCount / totalCount
    model.addAttribute("totalCount", countDto.getTotalCount());
    model.addAttribute("collected", countDto.getCollectedCount());

    return "kanji/kanjiCollection";
  }

  /**
   * 무한 스크롤로 page=2,3... 요청 시 AJAX로 partial HTML 반환
   */
  @GetMapping("/collectionAjax")
  @Operation(summary = "한자 목록 AJAX", description = "추가 10개 로딩")

  public String kanjiCollectionAjax(
      @AuthenticationPrincipal MyPrincipal principal,
      @ModelAttribute KanjiSearchRequest searchRequest,
      @RequestParam(name = "page", defaultValue = "1") int page,
      Model model) {

    // 다음 페이지 로드
    Page<KanjiSearchResponse> searchResponse = kanjiCollectionService.getMyCollection(principal.getUserId(),
        searchRequest, page);

    model.addAttribute("searchResponse", searchResponse.getContent());

    // Thymeleaf fragment만 반환
    return "kanji/kanjiCollectionFragment :: cardList";
  }

  @GetMapping("/details")
  @ResponseBody
  @Operation(summary = "한자 정보", description = "한자의 정보와 수집한 날자를 화면에 전송한다")
  public KanjiDetailResponse getKanjiDetails(
      @RequestParam(name = "kanjiId") Long kanjiId,
      @AuthenticationPrincipal MyPrincipal principal) {
    return kanjiCollectionService.getKanjiDetail(kanjiId, principal.getUserId());
  }

  @ResponseBody
  @PostMapping("/addCollection")
  public void addCollection(
      @RequestParam("kanjiId") Long kanjiId,
      @AuthenticationPrincipal MyPrincipal principal) {
    kanjiCollectionService.addCollection(kanjiId, principal.getUserId());
  }

  @GetMapping("/otherCollection")
  public String otherKanjiCollection(
      @RequestParam(name = "userId") Long userId,
      @ModelAttribute KanjiSearchRequest searchRequest,
      Model model) {
    int page = 1;

    Page<KanjiSearchResponse> searchResponse = kanjiCollectionService.getMyCollection(userId,
        searchRequest, page);

    model.addAttribute("searchResponse", searchResponse.getContent());
    ;

    return "kanji/otherKanjiCollection";
  }

  /**
   * 무한 스크롤로 page=2,3... 요청 시 AJAX로 partial HTML 반환
   */
  @GetMapping("/otherCollectionAjax")
  @Operation(summary = "프로필 한자 목록 AJAX", description = "추가 10개 로딩")
  public String kanjiCollectionAjax(
      @RequestParam(name = "userId") Long userId,
      @ModelAttribute KanjiSearchRequest searchRequest,
      @RequestParam(name = "page", defaultValue = "1") int page,
      Model model) {

    // 다음 페이지 로드
    Page<KanjiSearchResponse> searchResponse = kanjiCollectionService.getMyCollection(userId,
        searchRequest, page);

    model.addAttribute("searchResponse", searchResponse.getContent());

    // Thymeleaf fragment만 반환
    return "kanji/kanjiCollectionFragment :: cardList";
  }

  @ResponseBody
  @PostMapping("/wrongCountUp")
  public void wrongCountUp(
      @RequestParam("kanjiId") Long kanjiId,
      @AuthenticationPrincipal MyPrincipal principal) {
    kanjiCollectionService.wrongCountUp(kanjiId, principal.getUserId());
  }

  @ResponseBody
  @PostMapping("/wrongDelete")
  public void wrongDelete(@RequestParam("kanjiId") Long kanjiId,
      @AuthenticationPrincipal MyPrincipal principal) {
    kanjiCollectionService.wrongDelete(kanjiId, principal.getUserId());
  }

  @ResponseBody
  @PostMapping("/addBookMark")
  public void addBookMark(@RequestParam("kanjiId") Long kanjiId,
      @AuthenticationPrincipal MyPrincipal principal) {
    kanjiCollectionService.addBookMark(kanjiId, principal.getUserId());
  }

  @ResponseBody
  @PostMapping("/deleteBookMark")
  public void deleteBookMark(@RequestParam("kanjiId") Long kanjiId,
      @AuthenticationPrincipal MyPrincipal principal) {
    kanjiCollectionService.deleteBookMark(kanjiId, principal.getUserId());
  }
}

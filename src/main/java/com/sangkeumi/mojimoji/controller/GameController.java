package com.sangkeumi.mojimoji.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.sangkeumi.mojimoji.dto.game.*;
import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.service.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

@Controller
@RequestMapping("/game")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Game API", description = "게임 관련 API")
public class GameController {

    private final GameService gameService;
    private final KanjiCollectionService kanjiCollectionService;

    @GetMapping("/play")
    public String game(
            @RequestParam(name = "bookId", defaultValue = "-1") String bookId, // 쿼리파라미터는 js에서 Number(new URLSearchParams(window.location.search).get("bookId")) || -1; 로 찾음
            @AuthenticationPrincipal MyPrincipal principal,
            Model model) {
    model.addAttribute("bookmarkedKanjiList", kanjiCollectionService.getBookmarkedKanji(principal.getUserId()));

    return "game/gameplay";
    }

    @GetMapping("/start/{bookId}")
    @ResponseBody
    @Operation(summary = "게임 시작", description = "새로운 게임을 시작합니다.")
    public ResponseEntity<GameStartResponse> gameStart(
            @PathVariable("bookId") Long bookId,
            @AuthenticationPrincipal MyPrincipal principal) {
        return ResponseEntity.ok(gameService.gameStart(bookId, principal.getUserId()));
    }

    @PostMapping(value = "/send/{bookId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> sendMessage(
            @PathVariable("bookId") Long bookId,
            @RequestParam("message") String message) {
        return gameService.getChatResponseStream(bookId, message);
    }

    @GetMapping(value = "/hint/{bookId}")
    @ResponseBody
    public String gameHint(
            @PathVariable("bookId") Long bookId,
            @RequestParam("kanji") String kanji) {
        return gameService.getGameHint(bookId, kanji);
    }

    @GetMapping("/end/{bookId}")
    @ResponseBody
    public ResponseEntity<GameEndResponse> gameEnd(
            @PathVariable("bookId") Long bookId,
            @AuthenticationPrincipal MyPrincipal principal) {
        gameService.gameEnd(bookId);
        GameEndResponse gameEndResponse = new GameEndResponse(
                kanjiCollectionService.getKanjiQuiz(bookId, principal.getUserId()));

        return ResponseEntity.ok(gameEndResponse);
    }
}

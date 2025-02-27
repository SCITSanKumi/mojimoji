package com.sangkeumi.mojimoji.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.sangkeumi.mojimoji.dto.game.*;
import com.sangkeumi.mojimoji.dto.kanji.AddKanjiCollection;
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

    @GetMapping("/screen")
    public String game() {

        return "game/screen";
    }

    @GetMapping("/start/{bookId}")
    @ResponseBody
    @Operation(summary = "게임 시작", description = "새로운 게임을 시작합니다.")
    public ResponseEntity<GameStartResponse> gameStart(
            @PathVariable("bookId") Long bookId,
            @AuthenticationPrincipal MyPrincipal principal) {
        return ResponseEntity.ok(gameService.gameStart(bookId, principal));
    }

    @ResponseBody
    @PostMapping(value = "/send/{bookId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessage(
            @PathVariable("bookId") Long bookId,
            @RequestParam("message") String message) {
        return gameService.getChatResponseStream(bookId, message);
    }

    @ResponseBody
    @GetMapping("/state/{bookId}")
    public ResponseEntity<GameStateResponse> getGameState(@PathVariable("bookId") Long bookId) {
        return ResponseEntity.ok(gameService.getGameState(bookId));
    }

    @ResponseBody
    @GetMapping("/end/{bookId}")
    public ResponseEntity<GameEndResponse> gameEnd(@PathVariable("bookId") Long bookId) {
        return ResponseEntity.ok(gameService.gameEnd(bookId));
    }

    @ResponseBody
    @PostMapping("/addCollection")
    public void addCollection(
            @RequestParam("kanjiId") Long kanjiId,
            @AuthenticationPrincipal MyPrincipal principal) {
        kanjiCollectionService.addCollection(kanjiId, principal);
    }
}

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

    @GetMapping("/play")
    public String game(@RequestParam(name = "bookId", defaultValue = "-1") String bookId, Model model) {
        Long numericBookId;

        try {
            numericBookId = Long.parseLong(bookId);
        } catch (NumberFormatException e) {
            numericBookId = -1L; // 변환 실패 시 기본값 설정
        }

        model.addAttribute("bookId", numericBookId); // 숫자로 변환하여 모델에 추가

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

    @GetMapping("/end/{bookId}")
    @ResponseBody
    public ResponseEntity<GameEndResponse> gameEnd(@PathVariable("bookId") Long bookId) {
        return ResponseEntity.ok(gameService.gameEnd(bookId));
    }
}

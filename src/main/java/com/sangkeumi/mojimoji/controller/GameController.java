package com.sangkeumi.mojimoji.controller;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.sangkeumi.mojimoji.dto.game.MessageSendRequest;
import com.sangkeumi.mojimoji.service.GameService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/game")
@RequiredArgsConstructor
@Tag(name = "Game API", description = "게임 관련 API")
public class GameController {

    private final GameService gameService;

    @GetMapping("/screen")
    public String game() {
        return "game/screen";
    }

    @PostMapping("/start")
    @Operation(summary = "게임 시작", description = "새로운 게임을 시작합니다.")
    public ResponseEntity<Map<String, Object>> startGame() {
        Long bookId = gameService.startGame();

        return ResponseEntity.ok(Map.of(
            "bookId", bookId,
            "message", "게임이 시작되었습니다!"
        ));
    }

    @PostMapping("/send")
    @Operation(summary = "OpenAI 대답 요청", description = "OpenAI 대답 요청을 처리합니다.")
    public ResponseEntity<String> sendMessage(@RequestBody MessageSendRequest request) throws InterruptedException, ExecutionException {
        CompletableFuture<String> response = gameService.getChatResponse(request);

        return ResponseEntity.ok(response.get());
    }
}

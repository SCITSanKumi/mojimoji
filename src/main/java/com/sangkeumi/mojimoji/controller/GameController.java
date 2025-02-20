package com.sangkeumi.mojimoji.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.sangkeumi.mojimoji.service.GameService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;

    @GetMapping("/screen")
    public String game() {
        return "game/screen";
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startGame() {
        Long bookId = gameService.startGame();

        return ResponseEntity.ok(Map.of(
            "bookId", bookId,
            "message", "게임이 시작되었습니다!"
        ));
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody Map<String, Object> request) {
        if (!request.containsKey("bookId") || !request.containsKey("request")) {
            return ResponseEntity.badRequest().body("잘못된 요청입니다.");
        }

        try {
            Long bookId = Long.parseLong(request.get("bookId").toString());
            String message = request.get("request").toString();
            String response = gameService.getChatResponse(bookId, message);

            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("bookId 형식이 잘못되었습니다.");
        }
    }
}

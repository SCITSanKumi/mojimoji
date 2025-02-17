package com.sangkeumi.mojimoji.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.sangkeumi.mojimoji.entity.User;
import com.sangkeumi.mojimoji.service.GameService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;

    @GetMapping("/play")
    public String game() {
        return "/game/play";
    }

    @PostMapping("/send")
    @ResponseBody
    public String sendMessage(
        @RequestParam(name = "bookId") Long bookId,
        @RequestBody Map<String, String> request,
        Principal principal) {
        return gameService.getChatResponse(bookId, request.get("message"), principal.getName());
    }
}

package com.sangkeumi.mojimoji.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.sangkeumi.mojimoji.dto.game.MessageSendRequest;
import com.sangkeumi.mojimoji.dto.kanji.AddKanjiCollection;
import com.sangkeumi.mojimoji.entity.*;
import com.sangkeumi.mojimoji.service.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping("/game")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Game API", description = "게임 관련 API")
public class GameController {

    private final GameService gameService;
    private final UsedBookKanjiService usedBookKanjiService;
    private final KanjiService kanjiService;
    private final KanjiCollectionService kanjiCollectionService;

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

    @GetMapping("/quiz")
    public String quiz(Model model) {

        List<UsedBookKanji> usedBookkanjiList = usedBookKanjiService.selectAll();

        model.addAttribute("usedBookKanjiList", usedBookkanjiList);
        return "/game/quiz";
    }

    @ResponseBody
    @PostMapping("/quiz")
    public boolean quiz(@RequestParam(name = "korOnyomi") String korOnyomi,
            @RequestParam(name = "korKunyomi") String korKunyomi,
            @RequestParam(name = "kanjiId") Long kanjiId) {

        boolean result = kanjiService.checkAnswer(korOnyomi, korKunyomi, kanjiId);
        return result;
    }

    @ResponseBody
    @PostMapping("/addCollection")
    public boolean addCollection(@RequestParam(name = "kanjiId") Long kanjiId,
            @RequestParam(name = "userId") Long userId) {
        AddKanjiCollection addKanjiCollection = new AddKanjiCollection(userId, kanjiId);
        boolean result = kanjiCollectionService.addCollection(addKanjiCollection);

        // boolean result = true;
        return result;
    }
}

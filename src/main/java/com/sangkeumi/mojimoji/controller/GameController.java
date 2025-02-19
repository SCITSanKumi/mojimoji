package com.sangkeumi.mojimoji.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;
import com.sangkeumi.mojimoji.entity.Kanji;
import com.sangkeumi.mojimoji.entity.KanjiCollection;
import com.sangkeumi.mojimoji.entity.UsedBookKanji;
import com.sangkeumi.mojimoji.entity.User;
import com.sangkeumi.mojimoji.service.KanjiCollectionService;
import com.sangkeumi.mojimoji.service.KanjiService;
import com.sangkeumi.mojimoji.service.UsedBookKanjiService;
import com.sangkeumi.mojimoji.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/game")
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final UsedBookKanjiService usedBookKanjiService;
    private final KanjiService kanjiService;
    private final KanjiCollectionService kanjiCollectionService;
    private final UserService userService;

    @GetMapping("/play")
    public String game() {
        return "/game/play";
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

        Kanji kanji = kanjiService.getKanji(kanjiId);
        User user = userService.getUser(userId);

        KanjiCollection kanjiCollection = new KanjiCollection((long) 9, kanji, user,
                null, null);

        log.info("한자컬렉션{}", kanjiCollection.toString());

        boolean result = kanjiCollectionService.addCollection(kanjiCollection);

        // boolean result = true;
        return result;
    }
}

package com.sangkeumi.mojimoji.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.entity.Kanji;
import com.sangkeumi.mojimoji.entity.KanjiCollection;
import com.sangkeumi.mojimoji.entity.User;
import com.sangkeumi.mojimoji.service.KanjiCollectionService;
import com.sangkeumi.mojimoji.service.KanjiService;
import com.sangkeumi.mojimoji.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/kanji")
@RequiredArgsConstructor
@Slf4j
public class KanjiController {

    private final KanjiCollectionService kanjiCollectionService;
    private final UserService userService;
    private final KanjiService kanjiService;

    @GetMapping("/collection")
    public String kanjiCollection(@AuthenticationPrincipal MyPrincipal principal,
            @RequestParam(name = "category", defaultValue = "") String category,
            @RequestParam(name = "jlptRank", defaultValue = "") String jlptRank,
            @RequestParam(name = "kanjiSearch", defaultValue = "") String kanjiSearch,
            Model model) {
        if (principal != null && principal.getUserId() != null) {

            Long userId = principal.getUserId();
            User user = userService.getUser(userId);
            User notUser = userService.getUser((long) 7);

            List<KanjiCollection> kanjiCollection = kanjiCollectionService.getKanjiCollection(userId, category,
                    jlptRank,
                    kanjiSearch);

            List<KanjiCollection> myCollection = kanjiCollectionService.getMyCollection(user, category, jlptRank,
                    kanjiSearch);

            List<KanjiCollection> allKanjiCollection = kanjiCollectionService.getAllKanjiCollection(category, jlptRank,
                    kanjiSearch, notUser);

            List<Kanji> kanjiList = kanjiService.getKanjiList(category, jlptRank, kanjiSearch);

            model.addAttribute("category", category);
            model.addAttribute("jlptRank", jlptRank);
            model.addAttribute("kanjiSearch", kanjiSearch);
            model.addAttribute("kanjiCollection", kanjiCollection);
            model.addAttribute("kanjiList", kanjiList);
            model.addAttribute("user", user);
            model.addAttribute("myCollection", myCollection);
            model.addAttribute("allKanjiCollection", allKanjiCollection);
        }
        return "kanji/kanjiCollection";
    }

    @GetMapping("/detail")
    public String kanjiDetail() {
        return "kanji/kanjiDetail";
    }
}

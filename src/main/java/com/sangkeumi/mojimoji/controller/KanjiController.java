package com.sangkeumi.mojimoji.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sangkeumi.mojimoji.dto.kanji.myCollectionRequest;
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
            @RequestParam(name = "kanjiSort", defaultValue = "한자번호순") String kanjiSort,
            @RequestParam(name = "sortDirection") String sortDirection, Model model) {
        if (principal != null && principal.getUserId() != null) {

            Long userId = principal.getUserId();
            User user = userService.getUser(userId);
            Integer collected = 0;

            List<myCollectionRequest> myCollection = kanjiCollectionService.getMyCollection(userId, category, jlptRank,
                    kanjiSearch, kanjiSort, sortDirection);

            for (myCollectionRequest count : myCollection) {
                if (count.getIsCollected() == 1) {
                    collected++;
                }
            }

            model.addAttribute("myCollection", myCollection);
            model.addAttribute("category", category);
            model.addAttribute("jlptRank", jlptRank);
            model.addAttribute("kanjiSearch", kanjiSearch);
            model.addAttribute("kanjiSort", kanjiSort);
            model.addAttribute("sortDirection", sortDirection);
            model.addAttribute("collected", collected);
            return "kanji/kanjiCollection";
        }

        return "kanji/kanjiCollection";
    }

    @GetMapping("/detail")
    public String kanjiDetail(@RequestParam(name = "kanjiId") Long kanjiId, Model model) {
        Kanji kanji = kanjiService.getKanji(kanjiId);

        model.addAttribute("kanji", kanji);

        return "kanji/kanjiDetail";
    }
}

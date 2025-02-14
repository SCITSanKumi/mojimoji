package com.sangkeumi.mojimoji.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/kanji")
public class KanjiController {

    @GetMapping("/collection")
    public String collection() {
        return "/kanji/kanjiCollection";
    }

    @GetMapping("/detail")
    public String detail() {
        return "/kanji/kanjiDetail";
    }
}

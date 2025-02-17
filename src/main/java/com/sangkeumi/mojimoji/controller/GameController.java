package com.sangkeumi.mojimoji.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/game")
public class GameController {

    @GetMapping("/play")
    public String game() {
        return "/game/play";
    }
}

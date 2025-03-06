package com.sangkeumi.mojimoji.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping("/rank")
@Tag(name = "Rank API", description = "랭킹 관련 API")
public class RankController {

    @GetMapping("/rankpage")
    public String Rank() {
        return "rank/rankpage";
    }

}

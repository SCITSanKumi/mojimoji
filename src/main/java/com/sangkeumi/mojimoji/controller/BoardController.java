package com.sangkeumi.mojimoji.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/board")
public class BoardController {

    @GetMapping("/story/list")
    public String storyList() {
        return "/board/story/storyList";
    }

    @GetMapping("/story/detail")
    public String storyDetail() {
        return "/board/story/storyDetail";
    }

    @GetMapping("/myStory/list")
    public String myStoryList() {
        return "/board/myStory/myStoryList";
    }

    @GetMapping("/myStory/detail")
    public String myStoryDetail() {
        return "/board/myStory/myStoryDetail";
    }
}

package com.sangkeumi.mojimoji.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sangkeumi.mojimoji.dto.board.SharedStoriesListResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryContentResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryInfoResponse;
import com.sangkeumi.mojimoji.service.BoardService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Board API", description = "스토리 게시판 관련 API")
public class BoardController {

    private final BoardService boardService;

    /**
     * 공유된 스토리 목록을 보여주는 페이지를 반환하는 메서드
     * 
     * @param searchWord 검색어
     * @param searchItem 검색 대상, 기본값은 "title"
     * @param sortOption 정렬 옵션, 기본값은 "date"
     * @param model
     * @return
     */
    @GetMapping("/story/list")
    public String storyList(
            @RequestParam(name = "searchWord", required = false) String searchWord,
            @RequestParam(name = "searchItem", required = false, defaultValue = "title") String searchItem,
            @RequestParam(name = "sortOption", required = false, defaultValue = "date") String sortOption,
            Model model) {
        List<SharedStoriesListResponse> sharedStoryList = boardService.searchAndSortSharedBooks(searchWord, searchItem,
                sortOption);
        model.addAttribute("sharedStoryList", sharedStoryList);
        model.addAttribute("searchWord", searchWord);
        model.addAttribute("searchItem", searchItem);
        return "board/story/storyList";
    }

    /**
     * 공유된 스토리의 상세 페이지를 반환하는 메서드
     * 
     * @param bookId
     * @param model
     * @return
     */
    @GetMapping("/story/detail")
    public String storyDetail(@RequestParam(name = "bookId") Long bookId, Model model) {
        // 공유된 스토리의 내용 조회
        List<SharedStoryContentResponse> sharedStoryContent = boardService.getSharedStoryContent(bookId);

        // 공유된 스토리의 정보 조회
        SharedStoryInfoResponse sharedStoryInfo = boardService.getSharedStoryInfo(bookId);

        // 공유된 스토리의 내용과 정보를 모델에 추가
        model.addAttribute("sharedStoryContent", sharedStoryContent);
        model.addAttribute("sharedStoryInfo", sharedStoryInfo);

        return "board/story/storyDetail";
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

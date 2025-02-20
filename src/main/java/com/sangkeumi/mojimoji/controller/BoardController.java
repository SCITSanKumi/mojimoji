package com.sangkeumi.mojimoji.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sangkeumi.mojimoji.dto.board.SharedStoriesListResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryContentResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryInfoResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryReplyRequest;
import com.sangkeumi.mojimoji.dto.board.SharedStoryReplyResponse;
import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
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
     * 댓글 삭제
     * 
     * @param sharedBookReplyId
     */
    @DeleteMapping("/story/comment")
    @ResponseBody
    public void deleteComment(@RequestParam(name = "sharedBookReplyId") Long sharedBookReplyId) {
        boardService.deleteComment(sharedBookReplyId);
    }

    /**
     * 댓글 추가
     * 
     * @param principal
     * @param request
     * @return
     */
    @PostMapping("/story/comment")
    @ResponseBody
    public SharedStoryReplyResponse addComment(@AuthenticationPrincipal MyPrincipal principal,
            @RequestBody SharedStoryReplyRequest request) {
        // 컨트롤러에서 MyPrincipal을 주입받아 userId 추출
        Long userId = principal.getUserId();
        return boardService.addComment(userId, request);
    }

    /**
     * 댓글 목록 조회
     * 
     * @param sharedBookId
     * @return
     */
    @GetMapping("/story/comment")
    @ResponseBody
    public List<SharedStoryReplyResponse> getCommentsWithPagination(
            @RequestParam(name = "sharedBookId") Long sharedBookId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return boardService.getComments(sharedBookId, page, size);
    }

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
    public String storyDetail(@RequestParam(name = "bookId") Long bookId, Model model,
            @AuthenticationPrincipal MyPrincipal principal) {
        // 공유된 스토리의 정보 조회
        SharedStoryInfoResponse sharedStoryInfo = boardService.getSharedStoryInfo(bookId);

        // 로그인한 사용자가 없거나, 현재 글의 작성자와 다르면 조회수 증가 처리
        if (sharedStoryInfo != null) {
            Long authorId = sharedStoryInfo.userId();
            Long currentUserId = (principal != null) ? principal.getUserId() : null;
            if (currentUserId == null || !authorId.equals(currentUserId)) {
                boardService.incrementHitCount(bookId);
                // 조회수 증가 후, 새로 증가된 값을 반영하기 위해 다시 공유 스토리 정보 조회
                sharedStoryInfo = boardService.getSharedStoryInfo(bookId);
            }
        }

        // 공유된 스토리의 내용 조회
        List<SharedStoryContentResponse> sharedStoryContent = boardService.getSharedStoryContent(bookId);

        // 모델에 데이터 추가
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

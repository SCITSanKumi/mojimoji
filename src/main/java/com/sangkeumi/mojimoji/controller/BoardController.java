package com.sangkeumi.mojimoji.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.sangkeumi.mojimoji.dto.board.*;
import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.service.BoardService;

import io.swagger.v3.oas.annotations.Operation;
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
     * @param searchWord
     * @param searchItem
     * @param sortOption
     * @param model
     * @return
     */
    @GetMapping("/story/list")
    public String storyList(
            @RequestParam(name = "searchWord", required = false) String searchWord,
            @RequestParam(name = "searchItem", required = false, defaultValue = "title") String searchItem,
            @RequestParam(name = "sortOption", required = false, defaultValue = "date") String sortOption,
            Model model) {
        // 첫 페이지(0번 페이지)에서 8개만 가져오기
        List<SharedStoryListResponse> sharedStoryList = boardService.searchAndSortSharedBooks(searchWord, searchItem,
                sortOption, 0, 8);
        model.addAttribute("sharedStoryList", sharedStoryList);
        model.addAttribute("searchWord", searchWord);
        model.addAttribute("searchItem", searchItem);
        model.addAttribute("sortOption", sortOption);
        return "board/story/storyList";
    }

    /**
     * 공유된 스토리 AJAX용 엔드포인트 (페이지네이션)
     *
     * @param searchWord
     * @param searchItem
     * @param sortOption
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/story/ajaxList")
    @ResponseBody
    public List<SharedStoryListResponse> ajaxStoryList(
            @RequestParam(name = "searchWord", required = false) String searchWord,
            @RequestParam(name = "searchItem", required = false, defaultValue = "title") String searchItem,
            @RequestParam(name = "sortOption", required = false, defaultValue = "date") String sortOption,
            @RequestParam(name = "page", defaultValue = "1") int page, // 두번째 페이지부터 시작
            @RequestParam(name = "size", defaultValue = "8") int size) {
        return boardService.searchAndSortSharedBooks(searchWord, searchItem, sortOption, page, size);
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
        // principal이 null이면 (로그인하지 않은 경우) 로그인 페이지로 리다이렉트
        if (principal == null) {
            return "redirect:/user/login";
        }

        // 공유된 스토리의 정보 조회
        SharedStoryInfoResponse sharedStoryInfo = boardService.getSharedStoryInfo(bookId, principal.getUserId());

        if (sharedStoryInfo != null) {
            // 현재 사용자와 작성자가 다르면 조회수 증가 처리
            if (!sharedStoryInfo.userId().equals(principal.getUserId())) {
                boardService.incrementHitCount(bookId, principal.getUserId());
                sharedStoryInfo = boardService.getSharedStoryInfo(bookId, principal.getUserId());
            }
        }

        // 공유된 스토리의 내용 조회
        List<SharedStoryContentResponse> sharedStoryContent = boardService.getSharedStoryContent(bookId);

        // 모델에 데이터 추가
        model.addAttribute("sharedStoryContent", sharedStoryContent);
        model.addAttribute("sharedStoryInfo", sharedStoryInfo);
        log.info("ssss{}", sharedStoryInfo.liked());

        return "board/story/storyDetail";
    }

    /**
     * 공유된 스토리 추천 수 토글 메서드
     *
     * @param sharedBookId
     * @param principal
     * @return
     */
    @PostMapping("/story/like")
    @ResponseBody
    @Operation(summary = "추천 수", description = "공유된 스토리의 추천 수를 증가/감소")
    public ResponseEntity<?> toggleLike(@RequestParam("sharedBookId") Long sharedBookId,
            @AuthenticationPrincipal MyPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            SharedStoryInfoResponse updatedInfo = boardService.toggleLike(sharedBookId, principal.getUserId());
            return ResponseEntity.ok(updatedInfo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
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
        Long userId = principal.getUserId();
        return boardService.addComment(userId, request);
    }

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
     * 내 스토리 목록을 보여주는 페이지를 반환하는 메서드
     *
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/myStory/list")
    public String myStoryList(@AuthenticationPrincipal MyPrincipal principal, Model model) {
        // 첫 페이지(0번 페이지)에서 8개만 가져오기
        List<MyStoryListResponse> myStoryList = boardService.getMyBooksPaginated(principal.getUserId(), 0, 8);
        model.addAttribute("myStoryList", myStoryList);
        // 초기 렌더링 시 전체 목록이 아니라 첫 페이지 데이터만 보여줌
        return "board/myStory/myStoryList";
    }

    /**
     * 내 스토리 AJAX용 엔드포인트 (페이지네이션)
     *
     * @param page
     * @param size
     * @param principal
     * @return
     */
    @GetMapping("/myStory/ajaxList")
    @ResponseBody
    public List<MyStoryListResponse> ajaxMyStoryList(
            @RequestParam(name = "page", defaultValue = "1") int page, // 초기 렌더링 후 다음 페이지부터 (0페이지는 초기 렌더링)
            @RequestParam(name = "size", defaultValue = "8") int size,
            @AuthenticationPrincipal MyPrincipal principal) {
        Long userId = principal.getUserId();
        return boardService.getMyBooksPaginated(userId, page, size);
    }

    /**
     * 공유 버튼을 눌렀을 때
     * 해당 스토리가 본인 스토리인지 확인 후 공유 처리
     */
    @PostMapping("/myStory/share")
    @ResponseBody
    public SharedStoryInfoResponse shareStory(@RequestParam(name = "bookId") Long bookId,
            @AuthenticationPrincipal MyPrincipal principal) {
        Long userId = principal.getUserId();
        return boardService.shareBook(bookId, userId);
    }

    /**
     * 삭제 버튼을 눌렀을 때 처리
     * 본인의 스토리만 삭제할 수 있도록 확인 후 삭제
     */
    @DeleteMapping("/myStory/delete")
    @ResponseBody
    public String deleteMyStory(@RequestParam(name = "bookId") Long bookId,
            @AuthenticationPrincipal MyPrincipal principal) {
        Long userId = principal.getUserId();
        boardService.deleteMyBook(bookId, userId);
        return "삭제되었습니다.";
    }

    /**
     * 내 스토리의 상세 페이지를 반환하는 메서드
     *
     * @param bookId
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/myStory/detail")
    public String myStoryDetail(
            @RequestParam("bookId") Long bookId,
            @AuthenticationPrincipal MyPrincipal principal,
            Model model) {
        // 내 스토리 정보 조회
        MyStoryInfoResponse myStoryInfo = boardService.getMyStoryInfo(bookId, principal.getUserId());

        // 공유된 스토리인 경우, 공유 상세 페이지로 리다이렉트
        if (myStoryInfo.sharedBookId() != null) {
            return "redirect:/board/story/detail?bookId=" + bookId;
        } else {
            // 공유되지 않은 스토리인 경우, 내 스토리 상세 페이지 데이터를 조회 및 모델에 추가
            List<MyStoryContentResponse> myStoryContent = boardService.getMyStoryContent(bookId);
            model.addAttribute("myStoryInfo", myStoryInfo);
            model.addAttribute("myStoryContent", myStoryContent);
            return "board/myStory/myStoryDetail";
        }
    }

    /**
     * 다른 사람 스토리 AJAX용 엔드포인트 (페이지네이션)
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/otherStory/ajaxList")
    @ResponseBody
    public List<OtherStoryListResponse> ajaxOtherUserStoryList(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "8") int size) {
        return boardService.getStoriesByUserId(userId, page, size);
    }
}

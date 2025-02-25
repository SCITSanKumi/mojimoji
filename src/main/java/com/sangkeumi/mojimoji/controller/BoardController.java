package com.sangkeumi.mojimoji.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.sangkeumi.mojimoji.dto.board.MyStoryListResponse;
import com.sangkeumi.mojimoji.dto.board.MyStoryContentResponse;
import com.sangkeumi.mojimoji.dto.board.MyStoryInfoResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryListResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryContentResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryInfoResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryReplyRequest;
import com.sangkeumi.mojimoji.dto.board.SharedStoryReplyResponse;
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
        List<SharedStoryListResponse> sharedStoryList = boardService.searchAndSortSharedBooks(searchWord, searchItem, sortOption);
        model.addAttribute("sharedStoryList", sharedStoryList);
        model.addAttribute("searchWord", searchWord);
        model.addAttribute("searchItem", searchItem);
        return "board/story/storyList";
    }

    /**
     * 공유된 스토리의 상세 페이지를 반환하는 메서드
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
        SharedStoryInfoResponse sharedStoryInfo = boardService.getSharedStoryInfo(bookId);

        if (sharedStoryInfo != null) {
            // 현재 사용자와 작성자가 다르면 조회수 증가 처리
            if (!sharedStoryInfo.userId().equals(principal.getUserId())) {
                boardService.incrementHitCount(bookId, principal.getUserId());
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

    /**
     * 공유된 스토리 추천 수 토글 메서드
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
     * @param sharedBookReplyId
     */
    @DeleteMapping("/story/comment")
    @ResponseBody
    public void deleteComment(@RequestParam(name = "sharedBookReplyId") Long sharedBookReplyId) {
        boardService.deleteComment(sharedBookReplyId);
    }

    /**
     * 내 스토리 목록을 보여주는 페이지를 반환하는 메서드
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/myStory/list")
    public String myStoryList(Model model, @AuthenticationPrincipal MyPrincipal principal) {
        Long userId = principal.getUserId();
        List<MyStoryListResponse> myStoryList = boardService.getMyBooks(userId);
        model.addAttribute("myStoryList", myStoryList);
        return "board/myStory/myStoryList";
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
     * @param bookId
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/myStory/detail")
    public String myStoryDetail(@RequestParam("bookId") Long bookId, Model model,
            @AuthenticationPrincipal MyPrincipal principal) {
        Long userId = principal.getUserId();

        // 내 스토리 정보 조회
        MyStoryInfoResponse myStoryInfo = boardService.getMyStoryInfo(bookId, userId);

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
}

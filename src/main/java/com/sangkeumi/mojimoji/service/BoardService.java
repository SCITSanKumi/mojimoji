package com.sangkeumi.mojimoji.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.dto.board.*;
import com.sangkeumi.mojimoji.entity.*;
import com.sangkeumi.mojimoji.repository.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardService {

    private final SharedBookRepository sharedBookRepository;
    private final BookLineRepository bookLineRepository;
    private final UserRepository userRepository;
    private final SharedBookReplyRepository sharedBookReplyRepository;
    private final BookRepository bookRepository;
    private final SharedBookViewRepository sharedBookViewRepository;
    private final SharedBookLikeRepository sharedBookLikeRepository;

    /**
     * 페이지네이션을 적용한 공유 스토리 목록 검색 및 정렬 메서드
     * AJAX 요청 등에서 page, size 값을 받아서 무한 스크롤 구현에 사용
     * @param searchWord
     * @param searchItem
     * @param sortOption
     * @param page
     * @param size
     * @return
     */
    public List<SharedStoryListResponse> searchAndSortSharedBooks(String searchWord, String searchItem,
                    String sortOption, int page, int size) {
        Sort sort;
        if ("date".equalsIgnoreCase(sortOption)) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        } else if ("viewCount".equalsIgnoreCase(sortOption)) {
            sort = Sort.by(Sort.Direction.DESC, "hitCount");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "gaechu");
        }
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SharedBook> sharedBooksPage;

        if (searchWord != null && !searchWord.trim().isEmpty()) {
            if ("title".equalsIgnoreCase(searchItem)) { // 제목 검색
                sharedBooksPage = sharedBookRepository.findByBook_TitleContainingIgnoreCase(searchWord, pageable);
            } else { // 작성자(닉네임) 검색
                sharedBooksPage = sharedBookRepository.findByBook_User_NicknameContainingIgnoreCase(searchWord, pageable);
            }
        } else {
            sharedBooksPage = sharedBookRepository.findAll(pageable);
        }

        return sharedBooksPage.stream()
                        .map(sharedBook -> new SharedStoryListResponse(
                                sharedBook.getBook().getBookId(),
                                sharedBook.getBook().getTitle(),
                                sharedBook.getBook().getThumbnailUrl(),
                                sharedBook.getBook().getUser().getNickname(),
                                sharedBook.getBook().getUser().getProfileUrl(),
                                sharedBook.getHitCount(),
                                sharedBook.getGaechu(),
                                sharedBook.getCreatedAt()))
                        .collect(Collectors.toList());
    }

    /**
     * 공유된 스토리 내용을 조회하는 메서드
     * @param bookId
     * @return
     */
    public List<SharedStoryContentResponse> getSharedStoryContent(Long bookId) {
        // bookID를 기반으로 BookLine(Entity) 목록을 가져옴
        List<BookLine> bookLines = bookLineRepository.findByBook_BookIdOrderBySequenceAsc(bookId);
        // BookLine(Entity) -> SharedStroyContentResponse(DTO)로 변환
        return bookLines.stream()
                        .map(bookLine -> new SharedStoryContentResponse(
                                bookLine.getUserContent(),
                                bookLine.getGptContent()))
                        .collect(Collectors.toList());
    }

    /**
     * 공유된 스토리 정보를 조회하는 메서드
     * @param bookId
     * @return
     */
    public SharedStoryInfoResponse getSharedStoryInfo(Long bookId) {

        Optional<SharedBook> sharedBookOpt = sharedBookRepository.findByBook_bookId(bookId);

        if (sharedBookOpt.isPresent()) {
            SharedBook sharedBook = sharedBookOpt.get();
            SharedStoryInfoResponse storyInfo = SharedStoryInfoResponse.builder()
                    .sharedBookId(sharedBook.getSharedBookId())
                    .bookId(sharedBook.getBook().getBookId())
                    .userId(sharedBook.getBook().getUser().getUserId())
                    .title(sharedBook.getBook().getTitle())
                    .thumbnailUrl(sharedBook.getBook().getThumbnailUrl())
                    .nickname(sharedBook.getBook().getUser().getNickname())
                    .profileUrl(sharedBook.getBook().getUser().getProfileUrl())
                    .hitCount(sharedBook.getHitCount())
                    .gaechu(sharedBook.getGaechu())
                    .liked(false)
                    .build();
            return storyInfo;
        } else {
            return null;
        }
    }

    /**
     * 조회수 증가 (한 사용자당 1 증가)
     * @param bookId
     * @param userId
     */
    @Transactional
    public void incrementHitCount(Long bookId, Long userId) {
        Optional<SharedBook> sharedBookOpt = sharedBookRepository.findByBook_bookId(bookId);
        if (sharedBookOpt.isPresent()) {
            SharedBook sharedBook = sharedBookOpt.get();
            // 로그인한 사용자 엔티티 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            // 이미 조회 기록이 있는지 체크
            if (!sharedBookViewRepository.existsBySharedBookAndUser(sharedBook, user)) {
                SharedBookView view = SharedBookView.builder()
                    .sharedBook(sharedBook)
                    .user(user)
                    .build();
                sharedBookViewRepository.save(view);
                // 조회수 증가
                sharedBook.setHitCount(sharedBook.getHitCount() + 1);
                sharedBookRepository.save(sharedBook);
            }
        }
    }

    /**
     * 추천 토글 : 사용자가 공유된 스토리에 대해 추천 수를 증가/감소
     * @param sharedBookId
     * @param userId
     * @return
     */
    @Transactional
    public SharedStoryInfoResponse toggleLike(Long sharedBookId, Long userId) {
        SharedBook sharedBook = sharedBookRepository.findById(sharedBookId)
                        .orElseThrow(() -> new RuntimeException("공유된 스토리를 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 추천 여부 확인
        Optional<SharedBookLike> likeOpt = sharedBookLikeRepository.findBySharedBookAndUser(sharedBook, user);
        boolean liked;
        if (likeOpt.isPresent()) {
            // 이미 추천한 경우: 추천 취소
            sharedBookLikeRepository.delete(likeOpt.get());
            sharedBook.setGaechu(sharedBook.getGaechu() - 1);
            liked = false;
        } else {
            // 추천하지 않은 경우: 추천 추가
            SharedBookLike like = SharedBookLike.builder()
                    .sharedBook(sharedBook)
                    .user(user)
                    .build();
            sharedBookLikeRepository.save(like);
            sharedBook.setGaechu(sharedBook.getGaechu() + 1);
            liked = true;
        }
        sharedBookRepository.save(sharedBook);
        return SharedStoryInfoResponse.builder()
                .sharedBookId(sharedBook.getSharedBookId())
                .bookId(sharedBook.getBook().getBookId())
                .userId(sharedBook.getBook().getUser().getUserId())
                .title(sharedBook.getBook().getTitle())
                .thumbnailUrl(sharedBook.getBook().getThumbnailUrl())
                .nickname(sharedBook.getBook().getUser().getNickname())
                .profileUrl(sharedBook.getBook().getUser().getProfileUrl())
                .hitCount(sharedBook.getHitCount())
                .gaechu(sharedBook.getGaechu())
                .liked(liked)
                .build();
    }

    /**
     * 댓글 목록 조회
     * @param sharedBookId
     * @return
     */
    @Transactional
    public List<SharedStoryReplyResponse> getComments(Long sharedBookId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return sharedBookReplyRepository.findBySharedBook_SharedBookId(sharedBookId, pageable)
                .stream()
                .map(reply -> new SharedStoryReplyResponse(
                        reply.getSharedBookReplyId(),
                        reply.getSharedBook().getSharedBookId(),
                        reply.getUser().getUserId(),
                        reply.getUser().getNickname(),
                        reply.getContent(),
                        reply.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * 댓글 추가
     * @param userId
     * @param request
     * @return
     */
    @Transactional
    public SharedStoryReplyResponse addComment(Long userId, SharedStoryReplyRequest request) {
        // 전달받은 sharedBookId로 SharedBook 조회
        SharedBook sharedBook = sharedBookRepository.findById(request.sharedBookId())
                        .orElseThrow(() -> new RuntimeException("공유된 스토리를 찾을 수 없습니다."));

        // User 엔티티 조회
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("작성자를 찾을 수 없습니다."));

        // 댓글 Entity 생성 및 저장
        SharedBookReply reply = SharedBookReply.builder()
                .sharedBook(sharedBook)
                .user(user)
                .content(request.content())
                .build();
        SharedBookReply savedReply = sharedBookReplyRepository.save(reply);

        return new SharedStoryReplyResponse(
                savedReply.getSharedBookReplyId(),
                sharedBook.getSharedBookId(),
                user.getUserId(),
                user.getNickname(),
                savedReply.getContent(),
                savedReply.getCreatedAt());
    }

    /**
     * 댓글 삭제
     * @param sharedBookReplyId
     */
    @Transactional
    public void deleteComment(Long sharedBookReplyId) {
        if (!sharedBookReplyRepository.existsById(sharedBookReplyId)) {
            throw new RuntimeException("해당 댓글이 존재하지 않습니다.");
        }

        sharedBookReplyRepository.deleteById(sharedBookReplyId);
    }

    /**
     * 내 스토리를 페이징 처리하여 조회하는 메서드 (무한 스크롤)
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @Transactional
    public List<MyStoryListResponse> getMyBooksPaginated(Long userId, int page, int size) {
        // 최신 등록 순(내림차순)으로 정렬
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Book> bookPage = bookRepository.findByUser_UserId(userId, pageable);
        return bookPage.stream()
                        .map(book -> new MyStoryListResponse(
                                book.getBookId(),
                                book.getTitle(),
                                book.getThumbnailUrl(),
                                book.isEnded(),
                                book.getSharedBook() != null,
                                book.getUser().getNickname(),
                                book.getUser().getProfileUrl(),
                                book.getSharedBook() != null ? book.getSharedBook().getHitCount() : 0,
                                book.getSharedBook() != null ? book.getSharedBook().getGaechu() : 0,
                                book.getCreatedAt()))
                        .collect(Collectors.toList());
    }

    /**
     * 내 스토리 공유
     * @param bookId
     * @param userId
     * @return
     */
    @Transactional
    public SharedStoryInfoResponse shareBook(Long bookId, Long userId) {
        // 책 조회
        Book book = bookRepository.findById(bookId)
                        .orElseThrow(() -> new RuntimeException("해당 책을 찾을 수 없습니다."));
        // 본인의 책이 아니라면 공유 불가
        if (!book.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("자신의 책만 공유할 수 있습니다.");
        }
        // 이미 공유된 상태면 기존 정보 반환
        if (book.getSharedBook() != null) {
            return getSharedStoryInfo(bookId);
        }
        // 공유되지 않은 경우 SharedBook 엔티티 생성 후 저장
        SharedBook sharedBook = SharedBook.builder()
                .book(book)
                .hitCount(0)
                .gaechu(0)
                .build();
        sharedBookRepository.save(sharedBook);
        return getSharedStoryInfo(bookId);
    }

    /**
     * 내 스토리 삭제
     * @param bookId
     * @param userId
     */
    @Transactional
    public void deleteMyBook(Long bookId, Long userId) {
        // 책 조회
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("해당 책을 찾을 수 없습니다."));
        // 본인의 책이 아니라면 삭제 불가
        if (!book.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("자신의 책만 삭제할 수 있습니다.");
        }
        // Cascade 옵션이 설정되어 있으므로, 책 삭제 시 연관된 SharedBook도 삭제
        bookRepository.delete(book);
    }

    /**
     * 내 스토리 정보를 조회하는 메서드
     * @param bookId
     * @param userId
     * @return
     */
    @Transactional
    public MyStoryInfoResponse getMyStoryInfo(Long bookId, Long userId) {
        // 책 조회 및 소유자 검증
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("해당 책을 찾을 수 없습니다."));
        if (!book.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("자신의 책만 조회할 수 있습니다.");
        }

        // 공유 여부: sharedBook이 null이 아니면 공유된 상태
        Long sharedBookId = (book.getSharedBook() != null) ? book.getSharedBook().getSharedBookId() : null;

        return MyStoryInfoResponse.builder()
                        .sharedBookId(sharedBookId)
                        .bookId(book.getBookId())
                        .userId(book.getUser().getUserId())
                        .title(book.getTitle())
                        .thumbnailUrl(book.getThumbnailUrl())
                        .isEnded(book.isEnded())
                        .nickname(book.getUser().getNickname())
                        .profileUrl(book.getUser().getProfileUrl())
                        .hitCount(book.getSharedBook() != null ? book.getSharedBook().getHitCount() : 0)
                        .gaechu(book.getSharedBook() != null ? book.getSharedBook().getGaechu() : 0)
                        .build();
    }

    /**
     * 내 스토리 내용을 조회하는 메서드
     * @param bookId
     * @return
     */
    @Transactional
    public List<MyStoryContentResponse> getMyStoryContent(Long bookId) {
        List<BookLine> bookLines = bookLineRepository.findByBook_BookIdOrderBySequenceAsc(bookId);
        return bookLines.stream()
            .map(bookLine -> new MyStoryContentResponse(
                bookLine.getUserContent(),
                bookLine.getGptContent()))
            .collect(Collectors.toList());
    }

    public Long getBooksCount(Long userId) {
        return bookRepository.countByUserUserId(userId);
    }

    @Transactional
    public List<OtherStoryListResponse> getStoriesByUserId(Long userId, int page, int size) {
        // 최신 등록 순(내림차순)으로 정렬
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Book> bookPage = bookRepository.findByUser_UserId(userId, pageable);
        return bookPage.stream()
                .map(book -> new OtherStoryListResponse(
                        book.getBookId(),
                        book.getTitle(),
                        book.getThumbnailUrl(),
                        book.getSharedBook() != null,
                        book.getUser().getNickname(),
                        book.getUser().getProfileUrl(),
                        book.getSharedBook() != null ? book.getSharedBook().getHitCount() : 0,
                        book.getSharedBook() != null ? book.getSharedBook().getGaechu() : 0,
                        book.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public OtherProfileResponse getOtherProfile(Long userId) {

        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found for id: " + userId));
            return new OtherProfileResponse(
                    user.getUserId(),
                    user.getNickname(),
                    user.getEmail(),
                    user.getProfileUrl());
    }
}

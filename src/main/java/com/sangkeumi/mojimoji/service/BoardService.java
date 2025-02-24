package com.sangkeumi.mojimoji.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.dto.board.MyStoriesListResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoriesListResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryContentResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryInfoResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryReplyRequest;
import com.sangkeumi.mojimoji.dto.board.SharedStoryReplyResponse;
import com.sangkeumi.mojimoji.entity.Book;
import com.sangkeumi.mojimoji.entity.BookLine;
import com.sangkeumi.mojimoji.entity.SharedBook;
import com.sangkeumi.mojimoji.entity.SharedBookReply;
import com.sangkeumi.mojimoji.entity.User;
import com.sangkeumi.mojimoji.repository.BookLineRepository;
import com.sangkeumi.mojimoji.repository.BookRepository;
import com.sangkeumi.mojimoji.repository.SharedBookReplyRepository;
import com.sangkeumi.mojimoji.repository.SharedBookRepository;
import com.sangkeumi.mojimoji.repository.UserRepository;

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

        /**
         * 공유된 스토리 목록 검색 및 정렬 메서드
         * 
         * @param searchWord
         * @param searchItem
         * @param sortOption
         * @return
         */
        public List<SharedStoriesListResponse> searchAndSortSharedBooks(String searchWord, String searchItem,
                        String sortOption) {
                Sort sort;
                if ("date".equalsIgnoreCase(sortOption)) {
                        sort = Sort.by(Sort.Direction.DESC, "createdAt");
                } else if ("viewCount".equalsIgnoreCase(sortOption)) {
                        sort = Sort.by(Sort.Direction.DESC, "hitCount");
                } else {
                        sort = Sort.by(Sort.Direction.DESC, "gaechu");
                }

                List<SharedBook> sharedBooks;

                if (searchWord != null && !searchWord.trim().isEmpty()) {
                        // 검색어가 있는 경우
                        if ("title".equalsIgnoreCase(searchItem)) {
                                // 제목 기준 검색
                                sharedBooks = sharedBookRepository.findByBook_TitleContainingIgnoreCase(searchWord,
                                                sort);
                        } else { // 작성자(닉네임) 기준 검색
                                sharedBooks = sharedBookRepository
                                                .findByBook_User_NicknameContainingIgnoreCase(searchWord, sort);
                        }
                } else {
                        // 검색어가 없는 경우 전체 조회 (정렬 적용)
                        sharedBooks = sharedBookRepository.findAll(sort);
                }

                // SharedBook(Entity) -> SharedStroiesListResponse(DTO)로 변환
                return sharedBooks.stream()
                                .map(sharedBook -> new SharedStoriesListResponse(
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
         * 공유된 스토리를 전부 조회하는 메서드
         * 
         * @return
         */
        public List<SharedStoriesListResponse> findAllSharedBooks() {
                return searchAndSortSharedBooks("", "title", "recommendation");
        }

        /**
         * 공유된 스토리 내용을 조회하는 메서드
         * 
         * @param bookId
         * @return
         */
        public List<SharedStoryContentResponse> getSharedStoryContent(Long bookId) {
                // bookID를 기반으로 BookLine(Entity) 목록을 가져옴
                List<BookLine> bookLines = bookLineRepository.findByBook_BookIdOrderBySequenceAsc(bookId);
                // BookLine(Entity) -> SharedStroyContentResponse(DTO)로 변환
                return bookLines.stream()
                                .map(bookLine -> new SharedStoryContentResponse(
                                                bookLine.getContent()))
                                .collect(Collectors.toList());
        }

        /**
         * 공유된 스토리 정보를 조회하는 메서드
         * 
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
                                        .build();
                        return storyInfo;
                } else {
                        return null;
                }
        }

        /**
         * 댓글 목록 조회
         * 
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
         * 
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
         * 
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
         * 조회수 증가
         * 
         * @param bookId
         */
        @Transactional
        public void incrementHitCount(Long bookId) {
                Optional<SharedBook> optionalSharedBook = sharedBookRepository.findByBook_bookId(bookId);
                if (optionalSharedBook.isPresent()) {
                        SharedBook sharedBook = optionalSharedBook.get();
                        sharedBook.setHitCount(sharedBook.getHitCount() + 1);
                        sharedBookRepository.save(sharedBook);
                }
        }

        /**
         * 내 스토리를 전부 조회하는 메서드
         * 
         * @param userId
         * @return
         */
        @Transactional
        public List<MyStoriesListResponse> getMyBooks(Long userId) {
                List<Book> books = bookRepository.findByUser_UserId(userId);
                return books.stream()
                                .map(book -> new MyStoriesListResponse(
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

        /**
         * 내 스토리 공유
         * 
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
         * 
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
                // Cascade 옵션이 설정되어 있으므로, 책 삭제 시 연관된 SharedBook 도 삭제
                bookRepository.delete(book);
        }

}
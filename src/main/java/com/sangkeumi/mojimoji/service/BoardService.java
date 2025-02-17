package com.sangkeumi.mojimoji.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.dto.board.SharedStoriesListResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryContentResponse;
import com.sangkeumi.mojimoji.dto.board.SharedStoryInfoResponse;
import com.sangkeumi.mojimoji.entity.BookLine;
import com.sangkeumi.mojimoji.entity.SharedBook;
import com.sangkeumi.mojimoji.repository.BookLineRepository;
import com.sangkeumi.mojimoji.repository.SharedBookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardService {

        private final SharedBookRepository sharedBookRepository;
        private final BookLineRepository bookLineRepository;

        /**
         * 공유된 스토리를 전부 조회하는 메서드
         * 
         * @return
         */
        public List<SharedStoriesListResponse> findAllSharedBooks() {
                List<SharedBook> sharedStories = sharedBookRepository.findAll();
                // SharedBook(Entity) -> SharedStoriesListResponse(DTO)로 변환
                return sharedStories.stream()
                                .map(sharedBook -> new SharedStoriesListResponse(
                                                sharedBook.getBook().getBookId(), // Book 객체의 bookId
                                                sharedBook.getBook().getTitle(), // Book 객체의 title
                                                sharedBook.getBook().getThumbnailUrl(), // Book 객체의 thumbnailUrl
                                                sharedBook.getBook().getUser().getNickname(), // Book 객체의 User 객체의
                                                                                              // nickname
                                                sharedBook.getBook().getUser().getProfileUrl(), // Book 객체의 User 객체의
                                                                                                // profileUrl
                                                sharedBook.getHitCount(),
                                                sharedBook.getGaechu()))
                                .collect(Collectors.toList());
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
}
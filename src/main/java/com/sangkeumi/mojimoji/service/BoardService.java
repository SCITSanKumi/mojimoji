package com.sangkeumi.mojimoji.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
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
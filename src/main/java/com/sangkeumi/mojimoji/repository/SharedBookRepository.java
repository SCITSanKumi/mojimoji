package com.sangkeumi.mojimoji.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.entity.SharedBook;

public interface SharedBookRepository extends JpaRepository<SharedBook, Long> {
    // bookId에 해당하는 SharedBook을 조회
    Optional<SharedBook> findByBook_bookId(Long bookId);

    // 제목 검색
    Page<SharedBook> findByBook_TitleContainingIgnoreCase(String keyword, Pageable pageable);

    // 작성자(닉네임) 검색
    Page<SharedBook> findByBook_User_NicknameContainingIgnoreCase(String keyword, Pageable pageable);

    
}

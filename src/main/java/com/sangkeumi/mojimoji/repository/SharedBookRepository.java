package com.sangkeumi.mojimoji.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.entity.SharedBook;

public interface SharedBookRepository extends JpaRepository<SharedBook, Long> {
    // bookId에 해당하는 SharedBook을 조회
    Optional<SharedBook> findByBook_bookId(Long bookId);

    // 키워드가 포함된 제목을 검색하며, 주어진 정렬 옵션으로 결과를 정렬
    List<SharedBook> findByBook_TitleContainingIgnoreCase(String keyword, Sort sort);

    // 키워드가 포함된 작성자(닉네임)를 검색하며, 주어진 정렬 옵션으로 결과를 정렬
    List<SharedBook> findByBook_User_NicknameContainingIgnoreCase(String keyword, Sort sort);
}

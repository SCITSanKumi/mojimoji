package com.sangkeumi.mojimoji.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.entity.BookLine;

public interface BookLineRepository extends JpaRepository<BookLine, Long> {

    /**
     * bookId에 해당하는 BookLine 목록을 스토리의 순서(sequence) 오름차순으로 조회
     *
     * @param bookId
     * @return
     */
    List<BookLine> findByBook_BookIdOrderBySequenceAsc(Long bookId);

}

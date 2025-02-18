package com.sangkeumi.mojimoji.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.entity.SharedBook;

public interface SharedBookRepository extends JpaRepository<SharedBook, Long> {

    /**
     * bookId에 해당하는 SharedBook을 조회
     *
     * @param bookId
     * @return
     */
    Optional<SharedBook> findByBook_bookId(Long bookId);

}

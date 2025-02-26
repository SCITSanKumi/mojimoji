package com.sangkeumi.mojimoji.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sangkeumi.mojimoji.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Book의 user 필드(User 타입) 내의 userId 속성을 기준으로 조회
    Page<Book> findByUser_UserId(Long userId, Pageable pageable);

}

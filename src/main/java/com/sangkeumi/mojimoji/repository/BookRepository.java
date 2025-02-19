package com.sangkeumi.mojimoji.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sangkeumi.mojimoji.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}

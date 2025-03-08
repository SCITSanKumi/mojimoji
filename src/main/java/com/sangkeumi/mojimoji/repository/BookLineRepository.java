package com.sangkeumi.mojimoji.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sangkeumi.mojimoji.entity.Book;
import com.sangkeumi.mojimoji.entity.BookLine;

import java.util.List;

@Repository
public interface BookLineRepository extends JpaRepository<BookLine, Long> {
    List<BookLine> findTop10ByBookOrderBySequenceDesc(Book book);
    List<BookLine> findByBook_BookIdOrderBySequenceAsc(Long bookId);
}

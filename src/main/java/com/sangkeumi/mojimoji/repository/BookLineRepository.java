package com.sangkeumi.mojimoji.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sangkeumi.mojimoji.entity.Book;
import com.sangkeumi.mojimoji.entity.BookLine;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookLineRepository extends JpaRepository<BookLine, Long> {
    Optional<BookLine> findTopByBookOrderBySequenceDesc(Book book);
    List<BookLine> findTop10ByBookOrderBySequenceDesc(Book book);
    List<BookLine> findByBookOrderBySequenceAsc(Book book);
    List<BookLine> findByBook_BookIdOrderBySequenceAsc(Long bookId);
}

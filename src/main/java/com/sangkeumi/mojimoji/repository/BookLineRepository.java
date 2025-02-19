package com.sangkeumi.mojimoji.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.entity.BookLine;

public interface BookLineRepository extends JpaRepository<BookLine, Long> {

    /** jpa 리포지토리에 사전 정의된 형식으로 <> 활용해서 구조를 만들고, 그것을 상속하겠다.
     * Jparepository는 원래부터 있는 것. db를 조회, 찾고 , 수정하는 도구에 불과.
     * bookId에 해당하는 BookLine 목록을 스토리의 순서(sequence) 오름차순으로 조회
     *
     * @param bookId
     * @return
     */
    List<BookLine> findByBook_BookIdOrderBySequenceAsc(Long bookId);

}

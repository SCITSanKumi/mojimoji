package com.sangkeumi.mojimoji.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.entity.SharedBookReply;

public interface SharedBookReplyRepository extends JpaRepository<SharedBookReply, Long> {
    /** SharedBook의 ID에 해당하는 댓글(SharedBookReply) 목록을 페이지네이션(Pageable)을 적용하여 조회 */
    List<SharedBookReply> findBySharedBook_SharedBookId(Long sharedBookId, Pageable pageable);
}

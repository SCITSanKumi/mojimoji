package com.sangkeumi.mojimoji.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.entity.SharedBookReply;

public interface SharedBookReplyRepository extends JpaRepository<SharedBookReply, Long> {
    // SharedBook의 sharedBookId 기준으로 댓글 목록 조회
    List<SharedBookReply> findBySharedBook_SharedBookId(Long sharedBookId);
}

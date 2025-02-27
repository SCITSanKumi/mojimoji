package com.sangkeumi.mojimoji.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Community_Replies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityReply {
    /**
     * Primary Key, 댓글 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_reply_id")
    private Long communityReplyId;

    /**
     * 해당 댓글이 달린 게시글 (Community_Posts 테이블과 다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_post_id", nullable = false)
    private CommunityPost communityPost;

    /**
     * 댓글 작성자 (Users 테이블과 다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 댓글 내용
     */
    @Column(nullable = false, length = 2000)
    private String content;

    /**
     * 생성일시
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 최종 수정일시
     */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}

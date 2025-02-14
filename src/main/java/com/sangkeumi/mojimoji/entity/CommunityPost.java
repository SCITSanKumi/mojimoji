package com.sangkeumi.mojimoji.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Community_Posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityPost {
    /**
     * Primary Key, 커뮤니티 게시글 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_post_id")
    private Long communityPostId;

    /**
     * 게시글 작성자 (Users 테이블과 다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 게시글 제목
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 게시글 내용 (TEXT)
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 조회수
     */
    @Column(name = "hit_count", nullable = false)
    @Builder.Default
    private int hitCount = 0;

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

    // 양방향 관계

    /**
     * 게시글에 달린 댓글 목록 (Community_Replies 테이블과 일대다 관계)
     */
    @OneToMany(mappedBy = "communityPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityReply> communityReplies;
}

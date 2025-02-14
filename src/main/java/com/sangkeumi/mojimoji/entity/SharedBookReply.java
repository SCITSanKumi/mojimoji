package com.sangkeumi.mojimoji.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Shared_Book_Replies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedBookReply {
    /**
     * Primary Key, 공유된 책 댓글 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shared_book_reply_id")
    private Long sharedBookReplyId;

    /**
     * 해당 댓글이 달린 공유된 책 (Shared_Books 테이블과 다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_book_id", nullable = false)
    private SharedBook sharedBook;

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

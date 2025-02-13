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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Shared_Books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedBook {
    /**
     * Primary Key, 공유된 책 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shared_book_id")
    private Long sharedBookId;

    /**
     * 공유된 책의 식별자 (Books 테이블과 다대일 혹은 1:1 관계)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /**
     * 조회수
     */
    @Column(name = "hit_count", nullable = false)
    @Builder.Default
    private int hitCount = 0;

    /**
     * 추천수 (gaechu)
     */
    @Column(nullable = false)
    @Builder.Default
    private int gaechu = 0;

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
     * 공유된 책에 대한 댓글 목록 (Shared_Book_Replies 테이블과 일대다 관계)
     */
    @OneToMany(mappedBy = "sharedBook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharedBookReply> sharedBookReplies;
}

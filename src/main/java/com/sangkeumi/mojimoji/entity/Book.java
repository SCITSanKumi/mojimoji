package com.sangkeumi.mojimoji.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    /**
     * Primary Key, 책의 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    /**
     * 책을 작성한 유저 (Users 테이블과 다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 책 제목
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 책의 대표 이미지 URL
     */
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    /**
     * 책의 생성일시
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 책의 최종 수정일시
     */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // 양방향 관계

    /**
     * 책을 구성하는 줄들의 목록 (Book_Lines 테이블과 일대다 관계)
     * sequence 필드로 정렬하여 책의 순서대로 조회 가능
     */
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<BookLine> bookLines;

    /**
     * 책이 공유된 정보 (Shared_Books 테이블과 1:1 또는 1:N 관계; 여기서는 1:1로 가정)
     */
    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SharedBook sharedBook;
}

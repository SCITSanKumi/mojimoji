package com.sangkeumi.mojimoji.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Book_Lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookLine {
    /**
     * Primary Key, 각 줄의 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Long lineId;

    /**
     * 어느 책에 속하는지 (Books 테이블과 다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /**
     * 해당 줄의 내용 (사용자)
     */
    @Column(name = "user_content", columnDefinition = "TEXT")
    private String userContent;

    /**
     * 해당 줄의 내용 (GPT)
     */
    @Column(name = "gpt_content", columnDefinition = "TEXT")
    private String gptContent;

    /**
     * 플레이어의 hp를 나타내는 컬럼
     */
    private int hp;

    /**
     * 플레이어의 mp를 나타내는 컬럼
     */
    private int mp;

    /**
     * 플레이어의 mp를 나타내는 컬럼
     */
    @Column(name = "current_location")
    private String currentLocation;

    /**
     * 책 내에서의 순서를 나타내는 필드
     */
    @Column(name = "sequence", nullable = false)
    private int sequence;

    /**
     * 생성일시
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // 양방향 관계

    /**
     * 이 줄에서 사용된 한자들 (Used_Book_Kanjis 테이블과 일대다 관계)
     */
    @OneToMany(mappedBy = "bookLine", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<UsedBookKanji> usedBookKanjis;
}

package com.sangkeumi.mojimoji.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_lines")
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
     * 사용자의 대답인지 openAI의 대답인지 ("user", "assistant", 또는 "system")
     */
    @Column(nullable = false)
    private String role;

    /**
     * 해당 줄의 내용 (텍스트)
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 책 내에서의 순서를 나타내는 필드
     */
    @Column(nullable = false)
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

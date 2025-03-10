package com.sangkeumi.mojimoji.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Used_Book_Kanjis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UsedBookKanji {
    /**
     * Primary Key, 사용된 한자 항목 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "used_book_kanji_id")
    private Long usedBookKanjiId;

    /**
     * 한자가 사용된 책의 줄 (Book_Lines 테이블과 다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", nullable = false)
    private BookLine bookLine;

    /**
     * 사용된 한자 (Kanjis 테이블과 다대일 관계)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "kanji_id", nullable = false)
    private Kanji kanji;

    /**
     * 생성일시
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
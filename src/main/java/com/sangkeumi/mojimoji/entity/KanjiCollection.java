package com.sangkeumi.mojimoji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Kanji_Collections")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanjiCollection {
    /**
     * Primary Key, 컬렉션 항목 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kanji_collection_id")
    private Long kanjiCollectionId;

    /**
     * 수집된 한자 (Kanjis 테이블과 다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanji_id", nullable = false)
    @ToString.Exclude
    private Kanji kanji;

    /**
     * 이 컬렉션을 소유한 유저 (Users 테이블과 다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    /**
     * 즐겨찾기 여부
     */
    @Column(name = "bookmarked", nullable = false)
    @Builder.Default
    private int bookmarked = 0;

    /**
     * 수집된 횟수
     */
    @Column(name = "collected_count", nullable = false)
    @Builder.Default
    private int collectedCount = 0;

    /**
     * 오답 횟수
     */
    @Column(name = "wrong_count", nullable = false)
    @Builder.Default
    private int wrongCount = 0;

    /**
     * 생성일시
     */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 최종 수정일시
     */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    @UpdateTimestamp
    private LocalDateTime updatedAt = LocalDateTime.now();
}
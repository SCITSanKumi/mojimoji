package com.sangkeumi.mojimoji.entity;

import java.time.LocalDateTime;

import com.sangkeumi.mojimoji.dto.kanji.KanjiCollectionSummaryId;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Kanji_Collection_Summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KanjiCollectionSummary {

    // 복합키를 Embeddable 클래스로 관리
    @EmbeddedId
    private KanjiCollectionSummaryId id;

    @Column(name = "collection_count", nullable = false)
    private int collectionCount;

    @Column(name = "first_collected_at")
    private LocalDateTime firstCollectedAt;

    @Column(name = "last_collected_at")
    private LocalDateTime lastCollectedAt;

    /**
     * 복합키에 포함된 userId, kanjiId를
     * 
     * @MapsId를 통해 주 엔티티에 매핑
     */
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("kanjiId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanji_id", nullable = false)
    private Kanji kanji;
}

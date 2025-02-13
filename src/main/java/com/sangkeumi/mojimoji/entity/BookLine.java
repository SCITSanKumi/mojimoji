package com.sangkeumi.mojimoji.entity;

import java.time.LocalDateTime;

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
     * 책 내에서의 순서를 나타내는 필드
     */
    @Column(nullable = false)
    private int sequence;

    /**
     * 해당 줄의 내용 (텍스트)
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 해당 줄이 플레이된 턴 수
     */
    @Column(name = "played_turns", nullable = false)
    private int playedTurns;

    /**
     * 해당 줄이 책의 끝인지 여부
     */
    @Column(name = "is_ended", nullable = false)
    private boolean isEnded;

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
     * 이 줄에서 사용된 한자들 (Used_Book_Kanjis 테이블과 일대다 관계)
     */
    @OneToMany(mappedBy = "bookLine", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<UsedBookKanji> usedBookKanjis;
}

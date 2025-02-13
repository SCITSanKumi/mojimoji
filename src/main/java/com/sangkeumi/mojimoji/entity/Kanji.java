package com.sangkeumi.mojimoji.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Kanjis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kanji {
    @Id
    private Long kanjiId;

    private String jlptRank;
    private String category;

    @Column(nullable = false, length = 4)
    private String kanji;

    private String korOnyomi;
    private String korKunyomi;
    private String jpnOnyomi;
    private String jpnKunyomi;
    private String meaning;

}

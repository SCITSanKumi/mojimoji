package com.sangkeumi.mojimoji.dto.kanji;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QuizKanjiDTO {
    @EqualsAndHashCode.Include
    private Long kanjiId;
    private String kanji;
    private String jlptRank;
    private String category;
    private String korOnyomi;
    private String korKunyomi;
    private String jpnOnyomi;
    private String jpnKunyomi;
    private String meaning;
    private Integer bookmarked;
}

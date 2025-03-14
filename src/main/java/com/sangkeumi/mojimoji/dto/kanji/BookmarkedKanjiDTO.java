package com.sangkeumi.mojimoji.dto.kanji;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkedKanjiDTO {
    private Long kanjiId;
    private String kanji;
    private String korOnyomi;
    private String korKunyomi;
    private String jpnOnyomi;
    private String jpnKunyomi;
    private String meaning;
}

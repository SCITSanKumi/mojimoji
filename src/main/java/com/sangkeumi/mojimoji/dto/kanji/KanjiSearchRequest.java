package com.sangkeumi.mojimoji.dto.kanji;

public record KanjiSearchRequest(
        String category,
        String jlptRank,
        String kanjiSearch,
        String kanjiSort,
        String sortDirection) {
}
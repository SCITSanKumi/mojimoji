package com.sangkeumi.mojimoji.dto.kanji;

public record KanjiDTO(
                Long kanjiId,
                String kanji,
                String jlptRank,
                String category,
                String korOnyomi,
                String korKunyomi,
                String jpnOnyomi,
                String jpnKunyomi,
                String meaning) {
}

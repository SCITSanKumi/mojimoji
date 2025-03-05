package com.sangkeumi.mojimoji.dto.kanji;

public record KanjiDTO(
    Long kanjiId,
    String kanji,
    String korOnyomi,
    String korKunyomi
) {
}

package com.sangkeumi.mojimoji.dto.kanji;

import java.time.LocalDateTime;

public record KanjiDetailResponse(
        Long kanjiId,
        String jlptRank,
        String category,
        String kanji,
        String korOnyomi,
        String korKunyomi,
        String jpnOnyomi,
        String jpnKunyomi,
        String meaning,
        LocalDateTime obtainedAt // KanjiCollections에서 획득한 날짜
) {
}

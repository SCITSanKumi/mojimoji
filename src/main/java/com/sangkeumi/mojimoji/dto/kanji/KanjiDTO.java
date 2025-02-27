package com.sangkeumi.mojimoji.dto.kanji;

import com.sangkeumi.mojimoji.entity.Kanji;

public record KanjiDTO(
    Long kanjiId,
    String kanji,
    String korOnyomi,
    String korKunyomi
) {
    public KanjiDTO(Kanji kanji) {
        this(
            kanji.getKanjiId(),
            kanji.getKanji(),
            kanji.getKorOnyomi(),
            kanji.getJpnOnyomi()
        );
    }
}

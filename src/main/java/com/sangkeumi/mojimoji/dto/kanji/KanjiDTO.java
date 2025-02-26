package com.sangkeumi.mojimoji.dto.kanji;

import com.sangkeumi.mojimoji.entity.Kanji;

public record KanjiDTO(
    String kanji,
    String korOnyomi,
    String korKunyomi
) {
    public KanjiDTO(Kanji kanji) {
        this(kanji.getKanji(), kanji.getKorOnyomi(), kanji.getJpnOnyomi());
    }
}

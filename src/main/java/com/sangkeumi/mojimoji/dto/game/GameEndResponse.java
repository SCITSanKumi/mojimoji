package com.sangkeumi.mojimoji.dto.game;

import java.util.List;

import com.sangkeumi.mojimoji.dto.kanji.KanjiDTO;

public record GameEndResponse(
    List<KanjiDTO> kanjis
) {
}

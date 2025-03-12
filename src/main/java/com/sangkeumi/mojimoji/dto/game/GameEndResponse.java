package com.sangkeumi.mojimoji.dto.game;

import java.util.List;

import com.sangkeumi.mojimoji.dto.kanji.QuizKanjiDTO;

public record GameEndResponse(
    List<QuizKanjiDTO> kanjis
) {
}

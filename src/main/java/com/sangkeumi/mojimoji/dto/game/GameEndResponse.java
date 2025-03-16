package com.sangkeumi.mojimoji.dto.game;

import java.util.Set;

import com.sangkeumi.mojimoji.dto.kanji.QuizKanjiDTO;

public record GameEndResponse(
    Set<QuizKanjiDTO> kanjis // 중복 없애기 위해 Set으로 받음
) {
}

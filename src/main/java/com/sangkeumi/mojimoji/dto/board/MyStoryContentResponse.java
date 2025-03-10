package com.sangkeumi.mojimoji.dto.board;

public record MyStoryContentResponse(
    String userContent,
    String gptContent) implements FormattedDialogue {
}

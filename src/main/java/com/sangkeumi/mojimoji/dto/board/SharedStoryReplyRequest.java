package com.sangkeumi.mojimoji.dto.board;

public record SharedStoryReplyRequest(
    Long sharedBookId,
    String content) {
}

package com.sangkeumi.mojimoji.dto.game;

public record MessageSendRequest(
    Long bookId,
    String message
) {
}

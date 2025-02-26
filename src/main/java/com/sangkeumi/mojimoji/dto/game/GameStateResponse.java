package com.sangkeumi.mojimoji.dto.game;

public record GameStateResponse(
    Integer health,
    Boolean isEnded
) {
}

package com.sangkeumi.mojimoji.dto.game;

public record GameStateResponse(
    Integer hp,
    Integer mp,
    Boolean isEnded
) {
}

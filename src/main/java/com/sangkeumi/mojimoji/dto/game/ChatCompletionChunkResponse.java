package com.sangkeumi.mojimoji.dto.game;

import java.io.Serializable;
import java.util.List;

public record ChatCompletionChunkResponse(
    String id,
    List<Choices> choices) implements Serializable {

    public String getContent() {
        return choices.get(0).delta().content();
    }

    public record Choices(Delta delta) {
        public record Delta(String content) {}
    }
}

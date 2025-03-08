package com.sangkeumi.mojimoji.dto.game;

import java.io.Serializable;
import java.util.List;

public record ChatCompletionResponse(
    String id,
    List<Choice> choices) implements Serializable {

    public String getContent() {
        return choices != null && !choices.isEmpty() ? choices.get(0).message().content() : null;
    }

    public record Choice(Message message) {}
    public record Message(String content) {}
}

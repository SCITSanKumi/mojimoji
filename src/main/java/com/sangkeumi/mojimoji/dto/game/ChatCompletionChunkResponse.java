package com.sangkeumi.mojimoji.dto.game;

import java.io.Serializable;
import java.util.List;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of="id")
public class ChatCompletionChunkResponse implements Serializable {
    private String id;
    private List<Choices> choices;

    public String getContent() {
        return getChoices().get(0).getDelta().getContent();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choices{
        private Delta delta;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Delta{
            private String content;
        }
    }
}
package com.sangkeumi.mojimoji.dto.game;

import java.io.Serializable;
import java.util.List;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of="id")
@Getter
public class ChatCompletionChunkResponse implements Serializable {
    private String id;
    private List<Choices> choices;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
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
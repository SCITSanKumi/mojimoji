package com.sangkeumi.mojimoji.dto.board;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class SharedStoryContentResponse extends FormattedDialogue {
    public SharedStoryContentResponse(String userContent, String gptContent) {
        super(userContent, gptContent);
    }
}
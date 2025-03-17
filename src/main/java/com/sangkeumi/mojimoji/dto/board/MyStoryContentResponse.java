package com.sangkeumi.mojimoji.dto.board;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class MyStoryContentResponse extends FormattedDialogue {
    public MyStoryContentResponse(String userContent, String gptContent) {
        super(userContent, gptContent);
    }
}
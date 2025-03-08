package com.sangkeumi.mojimoji.dto.game;

import java.io.Serializable;
import java.util.List;

public record ImageGenerationResponse(
    long created,
    List<ImageData> data
) implements Serializable {

    public String getB64Json() {
        return data != null && !data.isEmpty() ? data.get(0).b64_json() : null;
    }

    public record ImageData(String b64_json) {}
}

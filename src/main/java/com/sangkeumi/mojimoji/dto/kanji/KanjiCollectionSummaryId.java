package com.sangkeumi.mojimoji.dto.kanji;

import java.io.Serializable;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class KanjiCollectionSummaryId implements Serializable {
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "kanji_id")
    private Long kanjiId;
}

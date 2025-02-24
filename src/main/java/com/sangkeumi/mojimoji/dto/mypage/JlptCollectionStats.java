package com.sangkeumi.mojimoji.dto.mypage;

public interface JlptCollectionStats {
    // JLPT 등급 (N1, N2, N3, N4, N5, ALL)
    String getJlptRank();

    // 해당 등급(또는 ALL)에서 내가 수집한 한자의 개수
    Long getCollected();

}

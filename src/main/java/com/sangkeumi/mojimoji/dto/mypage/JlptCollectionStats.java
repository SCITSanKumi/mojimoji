package com.sangkeumi.mojimoji.dto.mypage;

public interface JlptCollectionStats {
    String getJlptRank(); // JLPT 등급 (N1, N2, N3, N4, N5, 전체)
    Long getCollected(); // 해당 등급(또는 전체)에서 내가 수집한 한자의 개수
}

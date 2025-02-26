package com.sangkeumi.mojimoji.dto.mypage;

import java.time.LocalDate;

public interface DailyAcquisitionStats {

    // created_at의 날짜 부분
    LocalDate getAcquisitionDate();

    // 날짜별 개수
    Long getDailyCount();
}

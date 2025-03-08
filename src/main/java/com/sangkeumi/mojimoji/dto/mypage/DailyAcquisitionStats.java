package com.sangkeumi.mojimoji.dto.mypage;

import java.time.LocalDate;

public interface DailyAcquisitionStats {
    LocalDate getAcquisitionDate(); // created_at의 날짜 부분
    Long getDailyCount(); // 날짜별 개수
}

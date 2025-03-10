package com.sangkeumi.mojimoji.dto.mypage;

public interface CategoryCollectionSummary {
    Integer getTotalCategoryCount(); // 쿼리에서 SELECT 한 컬럼명과 매핑
    Integer getFullyCollectedCategoryCount();
}

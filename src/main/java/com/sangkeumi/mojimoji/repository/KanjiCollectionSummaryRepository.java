package com.sangkeumi.mojimoji.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.dto.kanji.KanjiCollectionSummaryId;
import com.sangkeumi.mojimoji.entity.KanjiCollectionSummary;

public interface KanjiCollectionSummaryRepository
        extends JpaRepository<KanjiCollectionSummary, KanjiCollectionSummaryId> {

}

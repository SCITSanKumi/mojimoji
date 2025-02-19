package com.sangkeumi.mojimoji.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.entity.KanjiCollection;

public interface KanjiCollectionsRepository extends JpaRepository<KanjiCollection, Long> {

}

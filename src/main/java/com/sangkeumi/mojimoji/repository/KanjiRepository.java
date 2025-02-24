package com.sangkeumi.mojimoji.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.entity.Kanji;

public interface KanjiRepository extends JpaRepository<Kanji, Long> {

}

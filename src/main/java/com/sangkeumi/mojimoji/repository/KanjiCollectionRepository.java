package com.sangkeumi.mojimoji.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sangkeumi.mojimoji.dto.kanji.myCollectionRequest;
import com.sangkeumi.mojimoji.entity.Kanji;
import com.sangkeumi.mojimoji.entity.KanjiCollection;
import com.sangkeumi.mojimoji.entity.User;

public interface KanjiCollectionRepository extends JpaRepository<KanjiCollection, Long> {

        List<KanjiCollection> findAllByUser(User user);

        Optional<KanjiCollection> findByUserAndKanji(User user, Kanji kanji);

}

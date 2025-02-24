package com.sangkeumi.mojimoji.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.entity.UsedBookKanji;
import com.sangkeumi.mojimoji.repository.UsedBookKanjiRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsedBookKanjiService {

    private final UsedBookKanjiRepository usedBookKanjiRepository;

    public List<UsedBookKanji> selectAll() {
        return usedBookKanjiRepository.findAll();

    }

}

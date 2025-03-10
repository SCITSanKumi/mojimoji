package com.sangkeumi.mojimoji.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangkeumi.mojimoji.config.GameConfiguraton;
import com.sangkeumi.mojimoji.dto.game.*;
import com.sangkeumi.mojimoji.dto.kanji.KanjiDTO;
import com.sangkeumi.mojimoji.entity.*;
import com.sangkeumi.mojimoji.repository.*;

import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameService {
    private final BookRepository bookRepository;
    private final BookLineRepository bookLineRepository;
    private final UsedBookKanjiRepository usedBookKanjiRepository;
    private final UserRepository userRepository;
    private final KanjiRepository kanjiRepository;
    private final GameAsyncService gameAsyncService;
    private final WebClient webClient;
    private final GameConfiguraton gameConfiguraton;
    // private final ObjectMapper objectMapper = new ObjectMapper();

    /** 게임 시작 메서드 */
    @Transactional
    public GameStartResponse gameStart(Long bookId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("ID가 존재하지 않습니다."));

        Book book = bookRepository.findById(bookId)
            .filter(b -> b.getUser().getUserId() == userId) // 사용자 검증(다른 사용자의 bookId를 URL에서 조작하여 요청한 경우)
            .orElseGet(() -> {
                Book newBook = bookRepository.saveAndFlush(Book.builder()
                    .user(user)
                    .title("새로운 모험")
                    .isEnded(false)
                    .build());

                // 초기 메시지 저장
                bookLineRepository.save(BookLine.builder()
                    .book(newBook)
                    .gptContent(gameConfiguraton.getIntroMessage())
                    // .hp(gameConfiguraton.getDefaultHP())
                    // .mp(gameConfiguraton.getDefaultMP())
                    // .currentLocation(gameConfiguraton.getDefaultLocation())
                    .sequence(0)
                    .build());

                return newBook;
            });

        return new GameStartResponse(
            book.getBookId(),
            bookLineRepository.findTopByBookOrderBySequenceDesc(book)
                .orElseThrow(() -> new RuntimeException("해당 스토리가 존재하지 않습니다."))
                .getGptContent());
    }

    public Flux<String> getChatResponseStream(Long bookId, String message) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("해당 bookId의 게임이 존재하지 않습니다."));

        List<BookLine> history = bookLineRepository.findTop10ByBookOrderBySequenceDesc(book);
        Collections.reverse(history);

        int currentSequence = history.isEmpty() ? 0 : history.get(history.size() - 1).getSequence() + 1;
        // int currentHP = history.isEmpty() ? 100 : history.get(history.size() - 1).getHp();
        // int currentMP = history.isEmpty() ? 100 : history.get(history.size() - 1).getMp();
        // String currentLocation = history.isEmpty() ? "" : history.get(history.size() - 1).getCurrentLocation();

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", gameConfiguraton.getSystemMessage()));
        history.forEach(msg -> messages.add(Map.of("role", "assistant", "content", msg.getGptContent())));
        messages.add(Map.of("role", "user", "content", message));

        log.info("gpt Request : {}", messages);

        // 사용자 입력 저장
        BookLine bookLine = bookLineRepository.save(BookLine.builder()
            .book(book)
            .userContent(message)
            .sequence(currentSequence)
            .build());


        saveUsedBookKanjis(bookLine, message);

        StringBuilder contentResponse = new StringBuilder();

        return getChatResponseFromApi(messages)
            .doOnNext(chunk -> contentResponse.append(chunk))
            .doOnComplete(() -> handleChatResponse(contentResponse.toString(), bookLine));
    }

    @Transactional
    public void saveUsedBookKanjis(BookLine bookLine, String message) {
        Set<String> kanjiSet = new HashSet<>();

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (Character.UnicodeBlock.of(c) != Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) continue;
            kanjiSet.add(String.valueOf(c));
        }

        if (kanjiSet.isEmpty()) {
            return;
        }

        List<Kanji> kanjiList = kanjiRepository.findByKanjiIn(kanjiSet);

        if (kanjiList.isEmpty()) {
            throw new RuntimeException("해당 한자가 존재하지 않습니다.");
        }
        List<UsedBookKanji> existingKanjiList = usedBookKanjiRepository.findByBookLineAndKanjiIn(bookLine, kanjiList);

        Set<String> existingKanjiSet = existingKanjiList.stream()
            .map(uk -> uk.getKanji().getKanji())
            .collect(Collectors.toSet());

        List<UsedBookKanji> newKanjiToSave = new ArrayList<>();

        for (Kanji kanji : kanjiList) {
            if (!existingKanjiSet.contains(kanji.getKanji())) {
                newKanjiToSave.add(UsedBookKanji.builder()
                    .bookLine(bookLine)
                    .kanji(kanji)
                    .build());
            }
        }

        if (!newKanjiToSave.isEmpty()) {
            usedBookKanjiRepository.saveAll(newKanjiToSave);
        }
    }


    @Transactional
    public GameEndResponse gameEnd(Long bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("해당 bookId의 게임이 존재하지 않습니다."));

        // 한자 정보 처리
        List<Kanji> kanjis = kanjiRepository.findKanjisUsedInBook(book);
        List<KanjiDTO> kanjiDTOs = kanjis.stream()
            .map(kanji -> new KanjiDTO(kanji.getKanjiId(), kanji.getKanji(), kanji.getKorOnyomi(), kanji.getKorKunyomi()))
            .collect(Collectors.toList());

        // 제목 및 썸네일 생성 후 저장 (비동기 실행)
        gameAsyncService.generateAndSaveBookDetails(book);

        return new GameEndResponse(kanjiDTOs);
    }


    @Transactional
    private void handleChatResponse(String content, BookLine bookLine) {
        log.info("gpt Response : {}", content);

        bookLine.setGptContent(content);
        bookLineRepository.save(bookLine);
    }

    // @Transactional
    // private void handleChatResponse(String content, BookLine bookLine) {
    //     log.info("gpt Response : {}", content);

    //     StringBuilder dialogue = new StringBuilder();

    //     // 정규식 패턴: {"name": "xxx"} 를 찾아서 "xxx :"로 변환
    //     Matcher matcher = Pattern.compile("\\{\"name\":\\s*\"([^\"]+)\"\\}").matcher(content);

    //     int lastEnd = 0;
    //     while (matcher.find()) {
    //         dialogue.append(content, lastEnd, matcher.start()); // 기존 텍스트 추가
    //         dialogue.append(matcher.group(1)).append(" : "); // "xxx :" 형식으로 변환
    //         lastEnd = matcher.end();
    //     }
    //     dialogue.append(content.substring(lastEnd)); // 남은 부분 추가

    //     // dialogue에서 JSON 부분 추출
    //     String dialogueText = dialogue.toString();
    //     Matcher jsonMatcher = Pattern.compile("\\{\\s*\"hp\"\\s*:\\s*\\d+.*?\\}").matcher(dialogueText);

    //     String extractedJson = null;
    //     if (jsonMatcher.find()) {
    //         extractedJson = jsonMatcher.group();
    //         // JSON 부분을 제외한 나머지 텍스트 추출
    //         dialogueText = dialogueText.replace(extractedJson, "").trim();
    //     }

    //     bookLine.setGptContent(dialogueText); // JSON 부분 제외한 텍스트 저장

    //     // JSON이 있는 경우 파싱하여 값 적용
    //     if (extractedJson != null) {
    //         try {
    //             Map<String, Object> extractedState = objectMapper.readValue(extractedJson, Map.class);

    //             bookLine.setHp((int) extractedState.getOrDefault("hp", 100));
    //             bookLine.setMp((int) extractedState.getOrDefault("mp", 100));
    //             bookLine.setCurrentLocation((String) extractedState.getOrDefault("current_location", ""));
    //             bookLine.getBook().setEnded((boolean) extractedState.getOrDefault("isEnded", false));
    //         } catch (JsonProcessingException e) {
    //             log.error("JSON 처리 중 오류 발생: {}", e.getMessage());
    //         }
    //     } else {
    //         log.warn("JSON 상태 정보가 없어 처리하지 않았습니다.");
    //     }

    //     // JSON을 제외한 대화 내용만 저장
    //     bookLineRepository.save(bookLine);
    //     bookRepository.save(bookLine.getBook());
    // }

    /** OpenAI API 스트리밍 요청 처리 */
    private Flux<String> getChatResponseFromApi(List<Map<String, String>> messages) {
        return webClient.post()
            .uri("https://api.openai.com/v1/chat/completions")
            .bodyValue(Map.of(
                "model", "gpt-4o-mini",
                "messages", messages,
                "temperature", 0.6,
                "max_tokens", 500,
                "stream", true))
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(ChatCompletionChunkResponse.class)
            .onErrorResume(error -> {
                if (error.getMessage().contains("JsonToken.START_ARRAY")) {
                    return Flux.empty();
                }
                return Flux.error(error);
            })
            .filter(response -> Optional.ofNullable(response.getContent())
                .map(String::trim)
                .filter(content -> !content.isEmpty())
                .isPresent())
            .map(response -> response.getContent());
    }



}

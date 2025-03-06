package com.sangkeumi.mojimoji.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangkeumi.mojimoji.config.GameConfiguraton;
import com.sangkeumi.mojimoji.dto.game.*;
import com.sangkeumi.mojimoji.dto.kanji.KanjiDTO;
import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.entity.*;
import com.sangkeumi.mojimoji.repository.*;

import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private final WebClient webClient;
    private final GameConfiguraton gameConfiguraton;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 게임 시작 메서드 */
    @Transactional
    public GameStartResponse gameStart(Long bookId, MyPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("ID가 존재하지 않습니다."));

        Book book = bookRepository.findById(bookId)
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
                            .hp(gameConfiguraton.getDefaultHP())
                            .mp(gameConfiguraton.getDefaultMP())
                            .currentLocation(gameConfiguraton.getDefaultLocation())
                            .turnCount(0)
                            .build());

                    return newBook;
                });

        return new GameStartResponse(book.getBookId(), gameConfiguraton.getIntroMessage()); //TODO 이어하기 만들려면 수정해야함
    }

    @Transactional
    public Flux<String> getChatResponseStream(Long bookId, String message) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("해당 bookId의 게임이 존재하지 않습니다."));

        List<BookLine> history = bookLineRepository.findTop10ByBookOrderByTurnCountDesc(book);
        Collections.reverse(history);

        int currentTurn = history.isEmpty() ? 0 : history.get(history.size() - 1).getTurnCount() + 1;
        int currentHP = history.isEmpty() ? 100 : history.get(history.size() - 1).getHp();
        int currentMP = history.isEmpty() ? 100 : history.get(history.size() - 1).getMp();
        String currentLocation = history.isEmpty() ? "" : history.get(history.size() - 1).getCurrentLocation();

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", gameConfiguraton.getSystemMessage()));
        history.forEach(msg -> messages.add(Map.of("role", "assistant", "content", msg.getGptContent())));
        messages.add(Map.of("role", "assistant", "content",
            String.format("[현재 플레이어 상태: 채력: %d 정신력: %d 위치: %s]", currentHP, currentMP, currentLocation)
        ));
        messages.add(Map.of("role", "user", "content", message));

        log.info("gpt Request : {}", messages);

        // 사용자 입력 저장
        BookLine bookLine = bookLineRepository.save(BookLine.builder()
                .book(book)
                .userContent(message)
                .turnCount(currentTurn)
                .build());

        // 사용자 입력 중 한자 파싱
        for (int i = 0; i < message.length(); i++) {
            // Kanji 엔티티를 찾기 전 한자인지 우선 검사
            if (!Character.UnicodeBlock.of(message.charAt(i))
            .equals(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)) {
                break;
            }

            // Kanji 엔티티를 DB에서 찾기
            Kanji kanji = kanjiRepository.findByKanji(String.valueOf(message.charAt(i)))
                    .orElseThrow(() -> new RuntimeException("해당 한자가 존재하지 않습니다."));

            // 사용자 입력 한자를 저장
            usedBookKanjiRepository.save(UsedBookKanji.builder()
                    .bookLine(bookLine)
                    .kanji(kanji)
                    .build());
        }

        StringBuilder contentResponse = new StringBuilder();

        return getChatResponseFromApi(messages)
                .doOnNext(chunk -> {contentResponse.append(chunk);})
                .doOnComplete(() -> {handleChatResponse(contentResponse.toString(), bookLine);});
    }


    @Transactional
    public GameEndResponse gameEnd(Long bookId) {
        List<Kanji> kanjis = kanjiRepository.findKanjisUsedInBook(bookId);
        List<KanjiDTO> kanjiDTOs = kanjis.stream()
                .map(kanji -> new KanjiDTO(kanji.getKanjiId(), kanji.getKanji(), kanji.getKorOnyomi(), kanji.getKorKunyomi()))
                .collect(Collectors.toList());

        return new GameEndResponse(kanjiDTOs);
    }

    @Transactional
    public void handleChatResponse(String content, BookLine bookLine) {
        log.info("gpt Response : {}", content);

        StringBuilder dialogue = new StringBuilder();

        // 정규식 패턴: {"name": "xxx"} 를 찾아서 "xxx :"로 변환
        Matcher matcher = Pattern.compile("\\{\"name\":\\s*\"([^\"]+)\"\\}").matcher(content); //TODO 간단한 방법으로

        int lastEnd = 0;
        while (matcher.find()) {
            dialogue.append(content, lastEnd, matcher.start()); // 기존 텍스트 추가
            dialogue.append(matcher.group(1)).append(" : "); // "xxx :" 형식으로 변환
            lastEnd = matcher.end();
        }
        dialogue.append(content.substring(lastEnd)); // 남은 부분 추가

        // dialogue에서 JSON 부분 추출
        String dialogueText = dialogue.toString();
        Matcher jsonMatcher = Pattern.compile("\\{\\s*\"hp\"\\s*:\\s*\\d+.*?\\}").matcher(dialogueText);

        String extractedJson = null;
        if (jsonMatcher.find()) {
            extractedJson = jsonMatcher.group();
            // JSON 부분을 제외한 나머지 텍스트 추출
            dialogueText = dialogueText.replace(extractedJson, "").trim();
        }

        // JSON이 있는 경우 파싱하여 값 적용
        if (extractedJson != null) {
            try {
                Map<String, Object> extractedState = objectMapper.readValue(extractedJson, Map.class);

                bookLine.setHp((int) extractedState.getOrDefault("hp", 100));
                bookLine.setMp((int) extractedState.getOrDefault("mp", 100));
                bookLine.setCurrentLocation((String) extractedState.getOrDefault("current_location", ""));
                bookLine.getBook().setEnded((boolean) extractedState.getOrDefault("isEnded", false));
            } catch (JsonProcessingException e) {
                log.error("JSON 처리 중 오류 발생: {}", e.getMessage());
            }
        } else {
            log.warn("JSON 상태 정보가 없어 처리하지 않았습니다.");
        }

        // JSON을 제외한 대화 내용만 저장
        bookLine.setGptContent(dialogueText); // JSON 부분 제외한 텍스트 저장
        bookLineRepository.save(bookLine); //TODO 없앨 수 있으면 없애기
        bookRepository.save(bookLine.getBook()); //TODO 없앨 수 있으면 없애기
    }

    /** OpenAI API 스트리밍 요청 처리 */
    private Flux<String> getChatResponseFromApi(List<Map<String, String>> messages) {
        return webClient.post()
                .bodyValue(Map.of(
                        "model", "gpt-4o-mini",
                        "temperature", 0.6,
                        "messages", messages,
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

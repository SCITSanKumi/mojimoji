package com.sangkeumi.mojimoji.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameService {
    private final int defaultHP = 100;
    private final int defaultMP = 100;

    private final BookRepository bookRepository;
    private final BookLineRepository bookLineRepository;
    private final UsedBookKanjiRepository usedBookKanjiRepository;
    private final UserRepository userRepository;
    private final KanjiRepository kanjiRepository;

    private final WebClient webClient;
    private final GameMessageProvider messageProvider;

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
                            .role("assistant")
                            .content(messageProvider.getIntroMessage())
                            .hp(defaultHP)
                            .mp(defaultMP)
                            .sequence(0)
                            .build());

                    return newBook;
                });

        return new GameStartResponse(book.getBookId(), messageProvider.getIntroMessage());
    }

    /** OpenAI API와 스트리밍 통신을 위한 Flux<String> */
    @Transactional
    public Flux<String> getChatResponseStream(Long bookId, String message) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("해당 bookId의 게임이 존재하지 않습니다."));

        List<BookLine> history = bookLineRepository.findTop10ByBookAndRoleOrderBySequenceDesc(book, "assistant");
        Collections.reverse(history);

        int lastSequence = history.isEmpty() ? 0 : history.get(history.size() - 1).getSequence();
        int currentHP = history.isEmpty() ? defaultHP : history.get(history.size() - 1).getHp();
        int currentMP = history.isEmpty() ? defaultMP : history.get(history.size() - 1).getMp();

        int nextSequence = lastSequence + 1;

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", messageProvider.getSystemMessage()));
        history.forEach(msg -> messages.add(Map.of("role", "assistant", "content", msg.getContent())));
        messages.add(Map.of("role", "assistant", "content", "현재 HP: " + currentHP + "현재 MP: " + currentMP));
        messages.add(Map.of("role", "user", "content", message));

        log.info("Messages: {}", messages);

        // 사용자 입력 저장
        BookLine bookLine = bookLineRepository.save(BookLine.builder()
                .book(book)
                .role("user")
                .content(message)
                .hp(currentHP)
                .mp(currentMP)
                .sequence(nextSequence)
                .build());

        for (int i = 0; i < message.length(); i++) {
            String kanjiCharacter = String.valueOf(message.charAt(i));

            if (!Character.UnicodeBlock.of(kanjiCharacter.charAt(0))
                    .equals(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)) {
                break;
            }

            // Kanji 엔티티를 DB에서 찾기
            Kanji kanji = kanjiRepository.findByKanji(kanjiCharacter)
                    .orElseThrow(() -> new RuntimeException("Kanji not found"));

            // 새 UsedBookKanji 객체를 생성하여 리스트에 추가
            UsedBookKanji usedBookKanji = UsedBookKanji.builder()
                    .bookLine(bookLine)
                    .kanji(kanji)
                    .build();

            usedBookKanjiRepository.save(usedBookKanji);
        }

        StringBuilder contentResponse = new StringBuilder();
        StringBuilder jsonResponse = new StringBuilder();
        AtomicBoolean isJsonStarted = new AtomicBoolean(false);

        return getChatResponseFromApi(messages)
                .doOnNext(chunk -> {
                    // JSON 시작 감지
                    if (!isJsonStarted.get() && chunk.trim().startsWith("{")) {
                        isJsonStarted.set(true); // JSON 감지 후 상태 변경
                    }

                    // JSON 데이터 수집
                    if (isJsonStarted.get()) {
                        jsonResponse.append(chunk);
                    } else {
                        contentResponse.append(chunk);
                    }
                })
                // .filter(chunk -> !isJsonStarted.get()) // JSON 데이터는 Flux에서 제외
                .doOnComplete(() -> {
                    int updatedHP = currentHP;
                    int updatedMP = currentMP;

                    try {
                        Map<String, Object> extractedState = new ObjectMapper().readValue(jsonResponse.toString(),
                                Map.class);

                        // 상태 업데이트
                        if (extractedState.containsKey("HP") && extractedState.get("HP") instanceof Integer) {
                            updatedHP = (int) extractedState.get("HP");
                        }

                        if (extractedState.containsKey("MP") && extractedState.get("MP") instanceof Integer) {
                            updatedMP = (int) extractedState.get("MP");
                        }

                        if (extractedState.containsKey("isEnded")
                                && extractedState.get("isEnded") instanceof Boolean
                                && (boolean) extractedState.get("isEnded")) {
                            book.setEnded(true);
                            bookRepository.save(book);
                        }
                    } catch (JsonProcessingException e) {
                        log.error("JSON 처리 중 오류 발생: ", e);
                    }

                    // 응답 저장 (JSON 제외)
                    bookLineRepository.save(BookLine.builder()
                            .book(book)
                            .role("assistant")
                            .content(contentResponse.toString().trim())
                            .hp(updatedHP)
                            .mp(updatedMP)
                            .sequence(nextSequence + 1)
                            .build());
                });
    }

    /** OpenAI API 스트리밍 요청 처리 */
    public Flux<String> getChatResponseFromApi(List<Map<String, String>> messages) {
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

    @Transactional
    public GameEndResponse gameEnd(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("해당 bookId의 게임이 존재하지 않습니다."));

        book.setEnded(true);

        List<Kanji> kanjis = kanjiRepository.findKanjisUsedInBook(bookId);

        List<KanjiDTO> kanjiDTOs = kanjis.stream()
                .map(kanji -> new KanjiDTO(kanji.getKanjiId(), kanji.getKanji(), kanji.getKorOnyomi(),
                        kanji.getKorKunyomi()))
                .collect(Collectors.toList());

        return new GameEndResponse(kanjiDTOs);
    }
}

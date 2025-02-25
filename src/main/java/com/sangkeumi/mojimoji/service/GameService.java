package com.sangkeumi.mojimoji.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangkeumi.mojimoji.dto.game.*;
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

import reactor.core.publisher.Flux;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameService {
    private final int defaultHealth = 100;

    private final BookRepository bookRepository;
    private final BookLineRepository bookLineRepository;
    private final UserRepository userRepository;
    private final GameMessageProvider messageProvider;
    private final WebClient webClient;

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
                    .build()
                );

                // 초기 메시지 저장
                bookLineRepository.save(BookLine.builder()
                    .book(newBook)
                    .role("assistant")
                    .content(messageProvider.getIntroMessage())
                    .health(defaultHealth)
                    .sequence(0)
                    .build()
                );

                return newBook;
            });

        return new GameStartResponse(book.getBookId(), messageProvider.getIntroMessage());
    }

    /** OpenAI API와 스트리밍 통신을 위한 Flux<String> */
    @Transactional
    public Flux<String> getChatResponseStream(MessageRequest request) {
        Book book = bookRepository.findById(request.bookId())
            .orElseThrow(() -> new RuntimeException("해당 bookId의 게임이 존재하지 않습니다."));

        List<BookLine> history = bookLineRepository.findTop10ByBookAndRoleOrderBySequenceDesc(book, "assistant");
        Collections.reverse(history);

        int lastSequence = history.isEmpty() ? 0 : history.get(history.size() - 1).getSequence();
        int currentHealth = history.isEmpty() ? defaultHealth : history.get(history.size() - 1).getHealth();

        int nextSequence = lastSequence + 1;

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", messageProvider.getSystemMessage()));
        history.forEach(msg -> messages.add(Map.of("role", "assistant", "content", msg.getContent())));
        messages.add(Map.of("role", "assistant", "content", "현재 체력: " + currentHealth));
        messages.add(Map.of("role", "user", "content", request.message()));

        log.info("Messages: {}", messages);

        // 사용자 입력 저장
        bookLineRepository.save(BookLine.builder()
            .book(book)
            .role("user")
            .content(request.message())
            .health(currentHealth)
            .sequence(nextSequence)
            .build()
        );

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
            .filter(chunk -> !isJsonStarted.get()) // JSON 데이터는 Flux에서 제외
            .doOnComplete(() -> {
                int updatedHealth = currentHealth;

                try {
                    Map<String, Object> extractedState = new ObjectMapper().readValue(jsonResponse.toString(), Map.class);

                    // 상태 업데이트
                    if (extractedState.containsKey("health") && extractedState.get("health") instanceof Integer) {
                        updatedHealth = (int)extractedState.get("health");
                    }

                    if (extractedState.containsKey("isEnded")
                            && extractedState.get("isEnded") instanceof Boolean
                            && (boolean)extractedState.get("isEnded")) {
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
                    .health(updatedHealth)
                    .sequence(nextSequence + 1)
                    .build()
                );
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
                "stream", true
            ))
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
                .isPresent()
            )
            .map(response -> response.getContent());
    }

    public GameStateResponse getGameState(Long bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("해당 bookId의 게임이 존재하지 않습니다."));

        BookLine bookLine = bookLineRepository.findTopByBookAndRoleOrderBySequenceDesc(book, "assistant")
            .orElseThrow(() -> new RuntimeException("해당 bookLine이 존재하지 않습니다."));

        return new GameStateResponse(bookLine.getHealth(), book.isEnded());
    }
}

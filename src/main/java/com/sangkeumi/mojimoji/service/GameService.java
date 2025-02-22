package com.sangkeumi.mojimoji.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.sangkeumi.mojimoji.dto.game.*;
import com.sangkeumi.mojimoji.entity.*;
import com.sangkeumi.mojimoji.repository.*;

import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameService {
    private final BookRepository bookRepository;
    private final BookLineRepository bookLineRepository;
    private final UserRepository userRepository;
    private final GameMessageProvider messageProvider;

    @Value("${openai.api-key}")
    private String openAiApiKey;
    private final WebClient webClient = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
        .baseUrl("https://api.openai.com/v1/chat/completions")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();

    /** 게임 시작 메서드 */
    @Transactional
    public Long startGame() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("ID가 존재하지 않습니다."));

        Book book = bookRepository.saveAndFlush(
            Book.builder()
                .user(user)
                .title("새로운 모험")
                .isEnded(false)
                .build()
        );

        // 초기 시스템 메시지 저장
        bookLineRepository.save(BookLine.builder()
            .book(book)
            .role("system")
            .content(messageProvider.getIntroMessage())
            .sequence(0)
            .build()
        );

        return book.getBookId();
    }

    /** OpenAI API와 스트리밍 통신을 위한 Flux<String> */
    public Flux<String> getChatResponseStream(MessageSendRequest request) {
        Book book = bookRepository.findById(request.bookId())
            .orElseThrow(() -> new RuntimeException("해당 bookId의 게임이 존재하지 않습니다."));

        List<BookLine> history = bookLineRepository.findTop10ByBookAndRoleOrderBySequenceDesc(book, "assistant");
        Collections.reverse(history);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", messageProvider.getSystemMessage()));
        history.forEach(msg -> messages.add(Map.of("role", "assistant", "content", msg.getContent())));
        messages.add(Map.of("role", "user", "content", request.message()));

        log.info("Messages: {}", messages);

        int nextSequence = bookLineRepository.findMaxSequenceByBook(book).orElse(0) + 1;

        // 사용자 입력 저장
        bookLineRepository.save(BookLine.builder()
            .book(book)
            .role("user")
            .content(request.message())
            .sequence(nextSequence)
            .build()
        );

        StringBuilder fullReply = new StringBuilder();

        // OpenAI API 스트리밍 응답을 가져오고, Flux<String> 형태로 반환
        return getChatResponseFromApi(messages)
            .doOnNext(chunk -> fullReply.append(chunk))
            .doOnComplete(() -> {
                bookLineRepository.save(BookLine.builder()
                    .book(book)
                    .role("assistant")
                    .content(fullReply.toString())
                    .sequence(nextSequence + 1)
                    .build()
                );
            });
    }

    /** OpenAI API 스트리밍 요청 처리 */
    public Flux<String> getChatResponseFromApi(List<Map<String, String>> messages) {
        return webClient.post()
            .header("Authorization", "Bearer " + openAiApiKey)
            .bodyValue(Map.of(
                "model", "gpt-4-turbo",
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
                else {
                    return Flux.error(error);
                }
            })
            .filter(response -> Optional.ofNullable(response.getChoices().get(0).getDelta().getContent())
                .map(String::trim)
                .filter(content -> !content.isEmpty())
                .isPresent()
            )
            .map(response -> response.getChoices().get(0).getDelta().getContent());
    }
}

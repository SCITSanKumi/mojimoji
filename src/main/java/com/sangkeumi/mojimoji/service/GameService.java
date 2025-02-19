package com.sangkeumi.mojimoji.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sangkeumi.mojimoji.entity.*;
import com.sangkeumi.mojimoji.repository.*;

import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameService {
    private final BookRepository bookRepository;
    private final BookLineRepository bookLineRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${openai.api-key}")
    private String openAiApiKey;
    private final String openAiUrl = "https://api.openai.com/v1/chat/completions";

    /**
     * 게임을 시작하는 메서드 (OpenAI 호출 없음)
     */
    @Transactional
    public Long startGame() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("ID가 존재하지 않습니다."));

        Book book = bookRepository.save(
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
            .content(generateGameIntroMessage())
            .sequence(0)
            .build()
        );

        return book.getBookId();
    }

    /**
     * 사용자 입력을 받아 OpenAI API와 상호작용하는 메서드
     * 비동기 호출을 사용하여 API 요청 처리
     */
    @Transactional
    public CompletableFuture<String> getChatResponse(Long bookId, String userMessage) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("해당 bookId의 게임이 존재하지 않습니다."));

        List<BookLine> history = bookLineRepository.findTop10ByBookAndRoleOrderBySequenceDesc(book, "assistant");
        Collections.reverse(history);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", generateCustomSystemMessage()));
        history.forEach(msg -> messages.add(Map.of("role", "assistant", "content", msg.getContent())));
        messages.add(Map.of("role", "user", "content", userMessage));

        log.info("Messages: {}", messages);

        // 비동기 호출
        CompletableFuture<String> assistantReply = getChatResponseFromApi(messages);

        int nextSequence = history.isEmpty() ? 1 : history.get(history.size() - 1).getSequence() + 2;

        bookLineRepository.save(BookLine.builder()
            .book(book)
            .role("user")
            .content(userMessage)
            .sequence(nextSequence)
            .build()
        );

        // 비동기 결과를 기다리기 전에 먼저 저장한 후 비동기 결과를 받음
        assistantReply.thenAccept(reply -> {
            bookLineRepository.save(BookLine.builder()
                .book(book)
                .role("assistant")
                .content(reply)
                .sequence(nextSequence + 1)
                .build()
            );
        });

        // 비동기 처리가 완료될 때까지 기다리거나 클라이언트에게 결과를 바로 반환하는 방법을 선택할 수 있음
        return assistantReply;  // 비동기 결과를 리턴
    }

    /**
     * OpenAI API와 상호작용하는 비동기 메서드
     */
    @Async  // 비동기 처리
    public CompletableFuture<String> getChatResponseFromApi(List<Map<String, String>> messages) {
        // OpenAI API 요청
        Map<String, Object> requestBody = Map.of(
            "model", "gpt-4-turbo",
            "messages", messages,
            "max_tokens", 300,
            "temperature", 0.5
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(openAiUrl, HttpMethod.POST, requestEntity, Map.class);

        Map<String, Object> responseBody = responseEntity.getBody();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return CompletableFuture.completedFuture((String) message.get("content"));
    }

    /**
     * 게임 시작 시의 안내 메시지 생성
     */
    private String generateGameIntroMessage() {
        return "용사의 모험이 시작됩니다! 한자를 입력하여 이야기를 진행하세요.";
    }

    /**
     * OpenAI API에 전달할 시스템 메시지 생성
     */
    private String generateCustomSystemMessage() {
        return """
            당신은 이 게임의 게임 마스터입니다. 사용자를 위한 특별한 모험을 안내해야 합니다.
            이 게임은 사용자가 한자 하나를 입력하면, 그 한자의 의미에 맞는 행동이 일어나고, 이야기가 전개됩니다.

            ## **게임 배경**
            사용자는 마왕성을 향해 가는 용사입니다. 그는 한자 하나로 세상과 상호작용하며, 목표는 마왕을 무찌르는 것입니다.

            ## **게임 규칙**
            1. 사용자가 한자 하나를 입력하면, 그 뜻에 맞는 게임 이벤트가 발생합니다.
            2. 전투, 퍼즐, 탐색 등 다양한 상황이 등장하며, 플레이어의 선택에 따라 스토리가 바뀝니다.
            3. 게임은 점점 더 위험해지며, 플레이어는 신중한 선택을 해야 합니다.

            ## **응답 형식**
            당신의 응답은 사용자의 선택을 반영한 흥미진진한 RPG 스토리를 서술하는 형식이어야 합니다.
            - 플레이어의 선택을 반영한 이야기 전개
            - 다음 행동을 유도하는 상황 제시

            예제:
            - 사용자가 '道(길 도)'를 입력하면:
              "너는 길을 찾기 위해 나아갔다. 앞에는 어두운 숲과 밝은 들판이 보인다. 어디로 갈 것인가?"

            - 사용자가 '戦(싸울 전)'을 입력하면:
              "너는 검을 뽑아 적과 맞섰다. 상대는 강력한 기사였다. 어떻게 싸울 것인가?"

            플레이어가 한자를 입력할 때마다, 이에 맞는 흥미로운 이야기를 만들어 주세요.
        """;
    }
}

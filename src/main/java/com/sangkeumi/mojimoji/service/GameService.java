package com.sangkeumi.mojimoji.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sangkeumi.mojimoji.entity.*;
import com.sangkeumi.mojimoji.repository.*;

import org.springframework.http.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameService {
    private final BookRepository bookRepository;
    private final BookLineRepository bookLineRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openai.api-key}")
    private final String openAiApiKey;
    private final String openAiUrl = "https://api.openai.com/v1/chat/completions";

    public String getChatResponse(Long bookId, String userMessage, String username) {
        // 사용자별 최신 시스템 메시지 조회(없으면 새로 생성)
        Book book =
            bookRepository.findById(bookId)
            .orElse(bookRepository.save(
                Book.builder()
                    .user(userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found")))
                    .build()
                ));
        BookLine systemMessage =
            bookLineRepository.findTopByBookAndRoleOrderByCreatedAtDesc(book, "system")
            .orElse(bookLineRepository.save(
                BookLine.builder()
                    .book(book)
                    .role("system")
                    .content(generateCustomSystemMessage())
                    .sequence(0) // 초기 시스템 메시지의 시퀀스 설정
                    .build()
                ));

        // 사용자 대화 내역 조회 (최근 10개만 가져옴)
        List<BookLine> history = bookLineRepository.findTop10ByBookAndRoleNotOrderByCreatedAtDesc(book, "system");
        Collections.reverse(history); // 최신 메시지가 먼저 오도록 정렬되어 있으므로 다시 뒤집음

        List<Map<String, String>> messages = new ArrayList<>();

        // 시스템 메시지 추가
        messages.add(Map.of("role", "system", "content", systemMessage.getContent()));

        // 기존 대화 내역 추가
        history.forEach(msg -> messages.add(Map.of("role", msg.getRole(), "content", msg.getContent())));

        // 사용자 메시지 추가
        messages.add(Map.of("role", "user", "content", userMessage));

        log.info("Messages: {}", messages);

        // OpenAI API 요청
        Map<String, Object> requestBody = Map.of(
            "model", "gpt-4-turbo",
            "messages", messages,
            "max_tokens", 500,
            "temperature", 0.75
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(
            openAiUrl, HttpMethod.POST, requestEntity, Map.class);

        // API 응답 검증 및 예외 처리
        Map<String, Object> responseBody = responseEntity.getBody();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String assistantReply = (String) message.get("content");

        // 사용자 입력과 AI 응답 저장
        int nextSequence = history.size() + 1; // 다음 시퀀스 번호 계산
        bookLineRepository.save(BookLine.builder().book(book).role("user").content(userMessage).sequence(nextSequence).build());
        bookLineRepository.save(BookLine.builder().book(book).role("assistant").content(assistantReply).sequence(nextSequence + 1).build());

        return assistantReply;
    }

    // 게임 마스터 역할을 부여하는 프롬프트
    private String generateCustomSystemMessage() {
        // 사용자에게 특정 설정을 기반으로 다른 시스템 메시지를 생성할 수 있음
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

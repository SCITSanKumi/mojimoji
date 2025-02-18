package com.sangkeumi.mojimoji.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sangkeumi.mojimoji.entity.*;
import com.sangkeumi.mojimoji.repository.*;

import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
     */
    @Transactional
    public String getChatResponse(Long bookId, String userMessage) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("해당 bookId의 게임이 존재하지 않습니다."));

        List<BookLine> history = bookLineRepository.findTop10ByBookAndRoleOrderBySequenceDesc(book, "assistant");
        Collections.reverse(history);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", generateCustomSystemMessage()));
        history.forEach(msg -> messages.add(Map.of("role", "assistant", "content", msg.getContent())));
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
        ResponseEntity<Map> responseEntity = restTemplate.exchange(openAiUrl, HttpMethod.POST, requestEntity, Map.class);

        Map<String, Object> responseBody = responseEntity.getBody();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String assistantReply = (String) message.get("content");

        int nextSequence = history.isEmpty() ? 1 : history.get(history.size() - 1).getSequence() + 2;

        bookLineRepository.save(BookLine.builder()
            .book(book)
            .role("user")
            .content(userMessage)
            .sequence(nextSequence)
            .build()
        );

        bookLineRepository.save(BookLine.builder()
            .book(book)
            .role("assistant")
            .content(assistantReply)
            .sequence(nextSequence + 1)
            .build()
        );

        return assistantReply;
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
            당신은 이 게임의 내레이터입니다. 사용자의 입력에 따라 이야기가 진행됩니다.
            사용자가 한자 하나를 입력하면, 해당 한자의 의미에 맞는 상황이 펼쳐집니다.

            예시:
            - '火'를 입력하면: "불길이 타오르며 길이 막혔다. 다른 길을 찾을 것인가?"
            - '水'를 입력하면: "시냇물을 발견했다. 마실 것인가, 건널 것인가?"
        """;
    }
}

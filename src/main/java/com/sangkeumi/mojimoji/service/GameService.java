package com.sangkeumi.mojimoji.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangkeumi.mojimoji.dto.game.MessageSendRequest;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import java.time.Duration;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameService {
    private final BookRepository bookRepository;
    private final BookLineRepository bookLineRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;

    @Value("${openai.api-key}")
    private String openAiApiKey;
    private final String openAiUrl = "https://api.openai.com/v1/chat/completions";

    /** 게임 시작 메서드 */
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

    /** OpenAI API와 스트리밍 통신을 위한 Flux<String> */
    public Flux<String> getChatResponseStream(Long bookId, String message) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("해당 bookId의 게임이 존재하지 않습니다."));

        List<BookLine> history = bookLineRepository.findTop10ByBookAndRoleOrderBySequenceDesc(book, "assistant");
        Collections.reverse(history);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", generateCustomSystemMessage()));
        history.forEach(msg -> messages.add(Map.of("role", "assistant", "content", msg.getContent())));
        messages.add(Map.of("role", "user", "content", message));

        log.info("Messages: {}", messages);

        int nextSequence = history.isEmpty() ? 1 : history.get(history.size() - 1).getSequence() + 2;

        // 사용자 입력 저장
        bookLineRepository.save(BookLine.builder()
            .book(book)
            .role("user")
            .content(message)
            .sequence(nextSequence)
            .build()
        );

        // OpenAI API 스트리밍 응답을 가져오고, Flux<String> 형태로 반환
        return getChatResponseFromApi(messages)
            .delayElements(Duration.ofMillis(50)) // 50ms 간격으로 데이터를 한 글자씩 보냄
            .doOnNext(reply -> bookLineRepository.save(
                BookLine.builder()
                    .book(book)
                    .role("assistant")
                    .content(reply)
                    .sequence(nextSequence + 1)
                    .build()
            ));
    }

    /** OpenAI API 스트리밍 요청 처리 */

public Flux<String> getChatResponseFromApi(List<Map<String, String>> messages) {
    return webClient.post()
        .uri(openAiUrl)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(Map.of(
            "model", "gpt-4-turbo",
            "messages", messages,
            "max_tokens", 500,
            "temperature", 0.6,
            "stream", true
        ))
        .retrieve()
        .bodyToFlux(String.class) // 🔹 문자열로 직접 받음
        .filter(response -> !response.equals("data: [DONE]")) // 🔹 "DONE" 메시지 무시
        .map(response -> response.replaceFirst("data:", "").trim()) // 🔹 "data:" 제거
        .filter(content -> !content.isEmpty()) // 🔹 빈 데이터 필터링
        .map(content -> {
            try {
                Map<String, Object> json = new ObjectMapper().readValue(content, Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) json.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                    if (delta != null && delta.containsKey("content")) {
                        return delta.get("content").toString();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // 🔹 JSON 파싱 오류 디버깅
            }
            return "";
        })
        .filter(content -> !content.isEmpty()) // 🔹 빈 값 필터링
        .bufferUntil(content -> content.endsWith(".") || content.endsWith(",") || content.endsWith("?") || content.endsWith("!")) // 🔹 문장 단위로 묶음
        .map(chunks -> {
            String joined = String.join("", chunks).trim();
            return joined.endsWith(" ") ? joined : joined + " "; // 🔹 문장 끝에 공백 추가
        })
        // .delayElements(Duration.ofMillis(150)) // 🔹 속도 조절
        .log(); // 🔹 디버깅용 로그 추가
}




    /** 게임 시작 안내 메시지 */
    private String generateGameIntroMessage() {
        return "용사의 모험이 시작됩니다! 한자를 입력하여 이야기를 진행하세요.";
    }

    /** OpenAI API에 전달할 시스템 메시지 생성 */
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

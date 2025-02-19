// package com.sangkeumi.mojimoji.service;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import com.sangkeumi.mojimoji.entity.*;
// import com.sangkeumi.mojimoji.repository.*;

// import jakarta.transaction.Transactional;
// import org.springframework.http.*;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// import java.util.*;

// @Service
// @Slf4j
// @RequiredArgsConstructor
// public class GameService {
//     private final BookRepository bookRepository;
//     private final BookLineRepository bookLineRepository;
//     private final UserRepository userRepository;
//     private final RestTemplate restTemplate = new RestTemplate();

//     @Value("${openai.api-key}")
//     private String openAiApiKey;
//     private final String openAiUrl = "https://api.openai.com/v1/chat/completions";

//     /**
//      * OpenAI API와의 상호작용 및 저장 로직
//      */
//     @Transactional
//     public String getChatResponse(Long bookId, String userMessage) {
//         Book book = getOrCreateBook(bookId);

//         // 최신 시스템 메시지 조회 또는 생성
//         BookLine systemMessage = bookLineRepository.findTopByBookAndRoleOrderBySequenceDesc(book, "system")
//             .orElse(bookLineRepository.save(BookLine.builder()
//                 .book(book)
//                 .role("system")
//                 .content(generateCustomSystemMessage())
//                 .sequence(0)
//                 .build()
//             ));

//         // 최근 10개 메시지 조회
//         List<BookLine> history = bookLineRepository.findTop10ByBookAndRoleOrderBySequenceDesc(book, "assistant");
//         Collections.reverse(history); // 최신 메시지가 먼저 오도록 정렬되어 있으므로 다시 뒤집음

//         List<Map<String, String>> messages = new ArrayList<>();
//         messages.add(Map.of("role", "system", "content", systemMessage.getContent()));
//         history.forEach(msg -> messages.add(Map.of("role", "assistant", "content", msg.getContent())));
//         messages.add(Map.of("role", "user", "content", userMessage));

//         log.info("Messages: {}", messages);

//         // OpenAI API 요청
//         Map<String, Object> requestBody = Map.of(
//             "model", "gpt-4-turbo",
//             "messages", messages,
//             "max_tokens", 500,
//             "temperature", 0.75
//         );

//         HttpHeaders headers = new HttpHeaders();
//         headers.set("Authorization", "Bearer " + openAiApiKey);
//         headers.setContentType(MediaType.APPLICATION_JSON);

//         HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//         ResponseEntity<Map> responseEntity =
//             restTemplate.exchange(openAiUrl, HttpMethod.POST, requestEntity, Map.class);

//         // 응답 파싱
//         Map<String, Object> responseBody = responseEntity.getBody();
//         List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
//         Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
//         String assistantReply = (String) message.get("content");

//         // 사용자 입력과 AI 응답 저장
//         int nextSequence = history.isEmpty() ? 1 : history.get(history.size() - 1).getSequence() + 2;

//         bookLineRepository.save(BookLine.builder()
//             .book(book)
//             .role("user")
//             .content(userMessage)
//             .sequence(nextSequence)
//             .build()
//         );

//         bookLineRepository.save(BookLine.builder()
//             .book(book)
//             .role("assistant")
//             .content(assistantReply)
//             .sequence(nextSequence + 1)
//             .build()
//         );

//         return assistantReply;
//     }

//     /**
//      * RW 트랜잭션에서 book 조회 또는 생성
//      */
//     @Transactional
//     private Book getOrCreateBook(Long bookId) throws UsernameNotFoundException {
//         Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

//         String username;
//         if (principal instanceof UserDetails) {
//             username = ((UserDetails) principal).getUsername(); // 로그인한 사용자 ID 반환
//         } else {
//             username = principal.toString();
//         }

//         return bookRepository.findById(bookId)
//             .orElseGet(() -> bookRepository.save(
//             Book.builder()
//                 .user(userRepository.findByUsername(username)
//                     .orElseThrow(() -> new UsernameNotFoundException("ID가 존재하지 않습니다.")))
//                 .title("default title")
//                 .isEnded(false)
//                 .build()
//             ));
//     }

//     /**
//      * 게임 마스터 역할을 부여하는 프롬프트
//      */
//     private String generateCustomSystemMessage() {
//         return """

//             """;
//     }
// }

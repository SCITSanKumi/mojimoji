package com.sangkeumi.mojimoji.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.sangkeumi.mojimoji.config.GameConfiguraton;
import com.sangkeumi.mojimoji.dto.game.*;
import com.sangkeumi.mojimoji.entity.*;
import com.sangkeumi.mojimoji.repository.*;

import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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

        // 메시지에서 한자 추출
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                kanjiSet.add(String.valueOf(c));
            }
        }

        if (kanjiSet.isEmpty()) {
            return;
        }

        // DB에서 한자 객체 조회
        List<Kanji> kanjiList = kanjiRepository.findByKanjiIn(kanjiSet);

        if (kanjiList.isEmpty()) {
            throw new RuntimeException("해당 한자가 존재하지 않습니다.");
        }

        // 기존에 저장된 UsedBookKanji를 book 기준으로 모두 조회
        List<UsedBookKanji> existingKanjiList = usedBookKanjiRepository.findByBook(bookLine.getBook());

        // 기존 저장된 한자들을 Set으로 만듦
        Set<String> existingKanjiSet = existingKanjiList.stream()
                .map(uk -> uk.getKanji().getKanji())
                .collect(Collectors.toSet());

        List<UsedBookKanji> newKanjiToSave = new ArrayList<>();

        // message에서 추출한 한자 중 아직 저장되지 않은 한자만 추가
        for (Kanji kanji : kanjiList) {
            log.info("kanji : {}", kanji.getKanji());
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
    public void gameEnd(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("해당 bookId의 게임이 존재하지 않습니다."));

        book.setEnded(true);

        // 제목 및 썸네일 생성 후 저장 (비동기 실행)
        // gameAsyncService.generateAndSaveBookDetails(book);
    }

    @Transactional
    private void handleChatResponse(String content, BookLine bookLine) {
        log.info("gpt Response : {}", content);

        bookLine.setGptContent(content);
        bookLineRepository.save(bookLine);
    }

    public String getGameHint(Long bookId, String kanji) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("해당 bookId의 게임이 존재하지 않습니다."));

        String content = bookLineRepository.findTopByBookOrderBySequenceDesc(book)
            .orElseThrow(() -> new RuntimeException("해당 스토리가 존재하지 않습니다."))
            .getGptContent();

        return generateDialogue(content, kanji);
    }

    private String generateDialogue(String content, String kanji) {
        // content와 한자 정보를 활용한 프롬프트 작성 (일본어)
        String prompt = "次のストーリーをもとに、以下の漢字を含む感動的で創造的な続きの内容を日本語で簡潔に作ってください：\n"
                + content
                + "\n【漢字】: " + kanji;

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "あなたは創造的な物語を作る専門家AIです。"));
        messages.add(Map.of("role", "user", "content", prompt));

        // 동기 방식으로 API 호출
        ChatCompletionResponse response = webClient.post()
            .uri("https://api.openai.com/v1/chat/completions")
            .bodyValue(Map.of(
                "model", "gpt-4o-mini",
                "messages", messages,
                "temperature", 0.7,
                "max_tokens", 150
            ))
            .retrieve()
            .bodyToMono(ChatCompletionResponse.class)
            .block();

        return Optional.ofNullable(response.getContent()).orElse("").trim();
    }

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

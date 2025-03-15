package com.sangkeumi.mojimoji.service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.sangkeumi.mojimoji.dto.game.*;
import com.sangkeumi.mojimoji.entity.*;
import com.sangkeumi.mojimoji.repository.*;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameAsyncService {
    private final BookRepository bookRepository;
    private final BookLineRepository bookLineRepository;
    private final WebClient webClient;

    @Value("${upload.path.image}")
    private String imagePath;

    /**
     * 줄거리 기반으로 제목 및 썸네일 생성 후 저장하는 비동기 메서드
     */
    @Async
    @Transactional
    public void generateAndSaveBookDetails(Book book) {
        List<BookLine> history = bookLineRepository.findByBookOrderBySequenceAsc(book);
        String combinedText = history.stream()
            .map(BookLine::getGptContent)
            .collect(Collectors.joining(" "));

        // 먼저 제목 생성
        generateTitle(combinedText)
            .flatMap(generatedTitle -> {
                book.setTitle(generatedTitle);
                return generateThumbnail(generatedTitle);
            })
            .flatMap(thumbnailUrl -> {
                book.setThumbnailUrl(thumbnailUrl);
                return Mono.fromCallable(() -> bookRepository.save(book))
                           .subscribeOn(Schedulers.boundedElastic());
            })
            .subscribe(
                success -> log.info("Book updated successfully"),
                error -> log.error("Error updating book: ", error)
            );
    }

    /**
     * AI를 이용해 제목을 생성하는 비동기 메서드
     */
    private Mono<String> generateTitle(String combinedText) {
        String prompt = "다음 줄거리를 참고하여 서식이나 부호 없이 감동적이고 창의적인 제목을 한 문장으로 만들어주세요:\n" + combinedText;
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "당신은 창의적인 제목을 만드는 전문가 AI입니다."));
        messages.add(Map.of("role", "user", "content", prompt));

        return webClient.post()
            .uri("https://api.openai.com/v1/chat/completions")
            .bodyValue(Map.of(
                "model", "gpt-4o-mini",
                "messages", messages,
                "temperature", 0.7,
                "max_tokens", 50
            ))
            .retrieve()
            .bodyToMono(ChatCompletionResponse.class)
            .map(response -> Optional.ofNullable(response.getContent()).orElse("").trim());
    }

    /**
     * AI를 이용해 썸네일 이미지를 생성하는 비동기 메서드
     */
    private Mono<String> generateThumbnail(String generatedTitle) {
        String prompt = "다음 제목을 밝고 화사한 색감, 또렷한 선의 캐릭터 중심 일러스트로 표현해주세요: " + generatedTitle;

        return webClient.post()
            .uri("https://api.openai.com/v1/images/generations")
            .bodyValue(Map.of(
                "model", "dall-e-3", // dall-e-3 모델 지정
                "prompt", prompt,
                "n", 1,
                "size", "1024x1024",
                "response_format", "b64_json"
            ))
            .retrieve()
            .bodyToMono(ImageGenerationResponse.class)
            .flatMap(response -> {
                String base64Image = response.getB64Json();
                if (base64Image == null) {
                    return Mono.error(new RuntimeException("이미지 생성 실패"));
                }
                String fileName = UUID.randomUUID().toString() + "_thumbnail";
                return saveImageLocallyFromBase64(base64Image, fileName);
            });
    }


    /**
     * base64 인코딩된 이미지 데이터를 받아서 로컬 서버에 저장하고, 저장된 파일의 경로를 반환하는 비동기 메서드
     */
    private Mono<String> saveImageLocallyFromBase64(String base64Image, String fileName) {
        return Mono.fromCallable(() -> {
            Path filePath = Paths.get(imagePath, "thumbnail_images", fileName + ".png");
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, Base64.getDecoder().decode(base64Image),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return "/uploads/image/thumbnail_images/" + fileName + ".png";
        }).subscribeOn(Schedulers.boundedElastic());
    }
}

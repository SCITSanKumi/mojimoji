package com.sangkeumi.mojimoji.config;

import org.springframework.http.HttpHeaders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.http.MediaType;
import reactor.netty.http.client.HttpClient;
@Configuration
public class WebClientConfig {
    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Bean
    public WebClient webClient (WebClient.Builder builder) {
        return builder
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
            .baseUrl("https://api.openai.com/v1/chat/completions")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Authorization", "Bearer " + openAiApiKey)
            .build();
    }
}

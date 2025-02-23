package com.sangkeumi.mojimoji.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync  // 비동기 처리를 활성화합니다.
public class AsyncConfig {
    // 필요한 경우 Executor 빈을 추가하여 스레드 풀을 구성할 수 있습니다.
}

package com.sangkeumi.mojimoji.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(info = @Info(title = "mojimoji API", version = "1.0", description = "mojimoji API Documentation"))
public class SwaggerConfig {

}

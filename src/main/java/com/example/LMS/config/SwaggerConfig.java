package com.example.LMS.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LMS API")
                        .version("1.0")
                        .description("API documentation for the Learning Management System"))
                .servers(List.of(new Server().url("https://lms-production-94cb.up.railway.app/")));
    }
}


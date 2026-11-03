package com.fitness.gateway.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${user.service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Bean
    public WebClient userServiceWebClient() {
        return WebClient.builder().baseUrl(userServiceUrl).build();
    }

}

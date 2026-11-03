package com.fitness.gateway.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final WebClient userServiceWebClient;

    public Mono<Boolean> validateUser(String userId) {
        log.info("Calling user service for {}", userId);
        return userServiceWebClient.get()
                .uri("/api/users/{userId}/validate", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .defaultIfEmpty(false)
                .onErrorResume(e -> {
                    log.error("Failed to validate user ID: {} - Error: {}", userId, e.getMessage());
                    return Mono.just(false);
                });
    }

    public Mono<UserResponse> registerUser(RegisterRequest request) {
        log.info("Calling user service to register user: {}", request.getEmail());
        return userServiceWebClient.post()
                .uri("/api/users/register")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if(e.getStatusCode() == HttpStatus.BAD_REQUEST)
                        return Mono.error(new RuntimeException("Bad request:"+e.getMessage()));

                    return Mono.error(new RuntimeException("Unexpected Error:"+e.getMessage()));
                });
    }

}

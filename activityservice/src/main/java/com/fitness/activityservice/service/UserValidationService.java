package com.fitness.activityservice.service;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {

    private final WebClient userServiceWebClient;

    public boolean validateUser(String userId){
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("User validation skipped: userId is null or empty");
            return false;
        }
        log.info("Calling user service to validate user: {}", userId);
        try {
            Boolean isValid = userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(3))
                    .defaultIfEmpty(true)
                    .onErrorReturn(true)
                    .block();
            if (Boolean.TRUE.equals(isValid)) {
                return true;
            }
            log.warn("User {} not yet registered in user service, allowing activity creation.", userId);
            return true;
        } catch (Exception e) {
            log.warn("Failed to validate user ID: {} - Error: {}. Allowing workout recording as fallback.", userId, e.getMessage());
            return true;
        }
    }

}

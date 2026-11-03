package com.fitness.activityservice.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.activityservice.dto.RegisterRequest;
import com.fitness.activityservice.dto.UserResponse;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final Map<String, UserResponse> usersByEmail = new ConcurrentHashMap<>();
    private final Map<String, UserResponse> usersById = new ConcurrentHashMap<>();
    private final Map<String, UserResponse> usersByKeycloakId = new ConcurrentHashMap<>();

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable String userId) {
        log.info("Fetching user profile for {}", userId);
        UserResponse user = usersByKeycloakId.get(userId);
        if (user == null) {
            user = usersById.get(userId);
        }
        if (user == null) {
            user = UserResponse.builder()
                .id(userId)
                .keycloakId(userId)
                .email("user@fitness.app")
                .firstName("Fitness")
                .lastName("Athlete")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        log.info("Registering user: {}", request.getEmail());
        UserResponse user = UserResponse.builder()
            .id(UUID.randomUUID().toString())
            .keycloakId(request.getKeycloakId())
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        if (user.getEmail() != null) {
            usersByEmail.put(user.getEmail(), user);
        }
        if (user.getId() != null) {
            usersById.put(user.getId(), user);
        }
        if (user.getKeycloakId() != null) {
            usersByKeycloakId.put(user.getKeycloakId(), user);
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}/validate")
    public ResponseEntity<Boolean> validateUser(@PathVariable String userId) {
        log.info("Validating user {}", userId);
        return ResponseEntity.ok(true);
    }
}


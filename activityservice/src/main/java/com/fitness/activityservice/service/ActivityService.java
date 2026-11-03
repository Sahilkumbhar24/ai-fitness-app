package com.fitness.activityservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.Recommendation;
import com.fitness.activityservice.repository.RecommendationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final KafkaTemplate<String, Activity> kafkaTemplate;
    private final ActivityAIService activityAIService;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationService recommendationService;

    private final Map<String, Activity> inMemoryActivities = new ConcurrentHashMap<>();

    private volatile boolean mongoHealthy = true;
    private volatile long lastMongoFailureTime = 0;
    private static final long MONGO_RETRY_INTERVAL_MS = 60000; // 1 minute

    @Value("${kafka.topic.name}")
    private String topicName;

    @Value("${spring.kafka.bootstrap-servers:}")
    private String kafkaBootstrapServers;

    public ActivityResponse trackActivity(ActivityRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new RuntimeException("Invalid user: userId is required");
        }

        // Validate user asynchronously in background so it never delays workout creation
        CompletableFuture.runAsync(() -> {
            try {
                userValidationService.validateUser(request.getUserId());
            } catch (Exception e) {
                log.warn("Background user validation skipped: {}", e.getMessage());
            }
        });

        Activity activity = Activity.builder()
            .id(UUID.randomUUID().toString())
            .userId(request.getUserId())
            .type(request.getType())
            .duration(request.getDuration())
            .caloriesBurned(request.getCaloriesBurned())
            .startTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now())
            .additionalMatrics(request.getAdditionalMatrics() != null ? request.getAdditionalMatrics() : request.getAdditionalMetrics())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // Resilient save: Try MongoDB only if healthy, fallback immediately to in-memory store
        Activity savedActivity = activity;
        boolean shouldTryMongo = mongoHealthy || (System.currentTimeMillis() - lastMongoFailureTime > MONGO_RETRY_INTERVAL_MS);
        if (shouldTryMongo) {
            try {
                savedActivity = activityRepository.save(activity);
                inMemoryActivities.put(savedActivity.getId(), savedActivity);
                mongoHealthy = true;
                log.info("Activity successfully saved to MongoDB Atlas: {}", savedActivity.getId());
            } catch (Exception e) {
                mongoHealthy = false;
                lastMongoFailureTime = System.currentTimeMillis();
                log.warn("MongoDB unavailable ({}), fast-falling back to in-memory store for user: {}", e.getMessage(), activity.getUserId());
                inMemoryActivities.put(activity.getId(), activity);
                savedActivity = activity;
            }
        } else {
            inMemoryActivities.put(activity.getId(), activity);
        }

        final Activity finalActivity = savedActivity;

        // Generate AI recommendation immediately in background so it's ready when user opens detail view
        CompletableFuture.runAsync(() -> {
            try {
                Recommendation rec = activityAIService.generateRecommendation(finalActivity);
                recommendationService.cacheRecommendation(rec);
                if (mongoHealthy) {
                    try {
                        recommendationRepository.save(rec);
                    } catch (Exception me) {
                        log.warn("Could not save AI recommendation to MongoDB: {}", me.getMessage());
                    }
                }
                log.info("AI Recommendation generated and cached for activity {}", finalActivity.getId());
            } catch (Exception e) {
                log.warn("Failed to generate AI recommendation: {}", e.getMessage());
            }
        });

        // Publish to Kafka asynchronously only if an external broker is configured
        if (kafkaBootstrapServers != null && !kafkaBootstrapServers.trim().isEmpty() && !kafkaBootstrapServers.contains("localhost")) {
            CompletableFuture.runAsync(() -> {
                try {
                    kafkaTemplate.send(topicName, finalActivity.getUserId(), finalActivity);
                    log.info("Activity event sent to Kafka topic '{}' for user {}", topicName, finalActivity.getUserId());
                } catch (Exception e) {
                    log.warn("Non-critical: Kafka publish skipped: {}", e.getMessage());
                }
            });
        }

        return mapToResponse(savedActivity);
    }

    private ActivityResponse mapToResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setType(activity.getType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMatrics(activity.getAdditionalMatrics());
        response.setCreatedAt(activity.getCreatedAt() != null ? activity.getCreatedAt() : LocalDateTime.now());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }

    public List<ActivityResponse> getUserActivities(String userId) {
        boolean shouldTryMongo = mongoHealthy || (System.currentTimeMillis() - lastMongoFailureTime > MONGO_RETRY_INTERVAL_MS);
        if (shouldTryMongo) {
            try {
                List<Activity> activities = activityRepository.findByUserId(userId);
                if (activities != null && !activities.isEmpty()) {
                    mongoHealthy = true;
                    activities.forEach(a -> inMemoryActivities.put(a.getId(), a));
                    return activities.stream().map(this::mapToResponse).collect(Collectors.toList());
                }
            } catch (Exception e) {
                mongoHealthy = false;
                lastMongoFailureTime = System.currentTimeMillis();
                log.warn("MongoDB query failed ({}), fast-falling back to in-memory store for user: {}", e.getMessage(), userId);
            }
        }

        // Fast fallback to in-memory store
        return inMemoryActivities.values().stream()
            .filter(a -> userId == null || userId.equals(a.getUserId()))
            .sorted((a, b) -> {
                LocalDateTime t1 = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
                LocalDateTime t2 = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
                return t2.compareTo(t1);
            })
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
}

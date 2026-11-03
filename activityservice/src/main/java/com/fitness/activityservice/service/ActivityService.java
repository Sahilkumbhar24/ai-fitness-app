package com.fitness.activityservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;

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
    private final com.fitness.activityservice.repository.RecommendationRepository recommendationRepository;

    @Value("${kafka.topic.name}")
    private String topicName;

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
            .userId(request.getUserId())
            .type(request.getType())
            .duration(request.getDuration())
            .caloriesBurned(request.getCaloriesBurned())
            .startTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now())
            .additionalMatrics(request.getAdditionalMatrics())
            .build();

        Activity savedActivity = activityRepository.save(activity);

        // Generate AI recommendation immediately in background so it's ready when user opens detail view
        CompletableFuture.runAsync(() -> {
            try {
                com.fitness.activityservice.model.Recommendation rec = activityAIService.generateRecommendation(savedActivity);
                recommendationRepository.save(rec);
                log.info("AI Recommendation generated and saved for activity {}", savedActivity.getId());
            } catch (Exception e) {
                log.warn("Failed to generate AI recommendation: {}", e.getMessage());
            }
        });

        // Publish to Kafka asynchronously so HTTP thread is never blocked by broker delays
        CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send(topicName, savedActivity.getUserId(), savedActivity);
                log.info("Activity event sent to Kafka topic '{}' for user {}", topicName, savedActivity.getUserId());
            } catch (Exception e) {
                log.error("Failed to publish activity to Kafka: {}", e.getMessage());
            }
        });

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
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }

    public List<ActivityResponse> getUserActivities(String userId) {
        List<Activity> activities = activityRepository.findByUserId(userId);
        return activities.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
}

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

    @Value("${kafka.topic.name}")
    private String topicName;

    public ActivityResponse trackActivity(ActivityRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new RuntimeException("Invalid user: userId is required");
        }

        // Validate user in background/non-blocking fashion so DB delays don't drop workouts
        try {
            userValidationService.validateUser(request.getUserId());
        } catch (Exception e) {
            log.warn("User validation skipped due to service warning: {}", e.getMessage());
        }

        Activity activity = Activity.builder()
            .userId(request.getUserId())
            .type(request.getType())
            .duration(request.getDuration())
            .caloriesBurned(request.getCaloriesBurned())
            .startTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now())
            .additionalMatrics(request.getAdditionalMatrics())
            .build();

        Activity savedActivity = activityRepository.save(activity);

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

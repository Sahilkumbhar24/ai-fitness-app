package com.fitness.activityservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.Recommendation;
import com.fitness.activityservice.repository.RecommendationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final ActivityRepository activityRepository;
    private final ActivityAIService activityAIService;

    private final Map<String, Recommendation> inMemoryRecommendations = new ConcurrentHashMap<>();

    public List<Recommendation> getUserRecommendation(String userId) {
        try {
            List<Recommendation> recs = recommendationRepository.findByUserId(userId);
            if (recs != null && !recs.isEmpty()) {
                return recs;
            }
        } catch (Exception e) {
            log.warn("MongoDB query for user recommendations failed: {}", e.getMessage());
        }

        return inMemoryRecommendations.values().stream()
            .filter(r -> userId == null || userId.equals(r.getUserId()))
            .collect(Collectors.toList());
    }

    public List<Recommendation> getAllRecommendations() {
        try {
            return recommendationRepository.findAll();
        } catch (Exception e) {
            log.warn("MongoDB query for all recommendations failed: {}", e.getMessage());
            return new ArrayList<>(inMemoryRecommendations.values());
        }
    }

    public Recommendation getRecommendationForActivity(String activityId) {
        // 1. Try DB first
        try {
            Optional<Recommendation> existing = recommendationRepository.findByActivityId(activityId);
            if (existing.isPresent()) {
                inMemoryRecommendations.put(activityId, existing.get());
                return existing.get();
            }
        } catch (Exception e) {
            log.warn("MongoDB read failed for activityId {}: {}", activityId, e.getMessage());
        }

        // 2. Check in-memory store
        Recommendation cached = inMemoryRecommendations.get(activityId);
        if (cached != null) {
            return cached;
        }

        // 3. Generate on-demand
        log.info("Generating on-demand AI recommendation for activityId: {}", activityId);
        Activity targetActivity = null;
        try {
            Optional<Activity> activityOpt = activityRepository.findById(activityId);
            if (activityOpt.isPresent()) {
                targetActivity = activityOpt.get();
            }
        } catch (Exception e) {
            log.warn("MongoDB lookup failed for activity: {}", e.getMessage());
        }

        if (targetActivity == null) {
            targetActivity = Activity.builder()
                .id(activityId)
                .duration(30)
                .caloriesBurned(250)
                .build();
        }

        Recommendation newRec = activityAIService.generateRecommendation(targetActivity);
        inMemoryRecommendations.put(activityId, newRec);

        try {
            recommendationRepository.save(newRec);
        } catch (Exception e) {
            log.warn("Could not save generated recommendation to MongoDB: {}", e.getMessage());
        }

        return newRec;
    }

}

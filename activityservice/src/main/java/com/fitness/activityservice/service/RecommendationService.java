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

    private volatile boolean mongoHealthy = true;
    private volatile long lastMongoFailureTime = 0;
    private static final long MONGO_RETRY_INTERVAL_MS = 60000;

    public List<Recommendation> getUserRecommendation(String userId) {
        boolean shouldTryMongo = mongoHealthy || (System.currentTimeMillis() - lastMongoFailureTime > MONGO_RETRY_INTERVAL_MS);
        if (shouldTryMongo) {
            try {
                List<Recommendation> recs = recommendationRepository.findByUserId(userId);
                if (recs != null && !recs.isEmpty()) {
                    mongoHealthy = true;
                    return recs;
                }
            } catch (Exception e) {
                mongoHealthy = false;
                lastMongoFailureTime = System.currentTimeMillis();
                log.warn("MongoDB query for user recommendations failed: {}", e.getMessage());
            }
        }

        return inMemoryRecommendations.values().stream()
            .filter(r -> userId == null || userId.equals(r.getUserId()))
            .collect(Collectors.toList());
    }

    public List<Recommendation> getAllRecommendations() {
        boolean shouldTryMongo = mongoHealthy || (System.currentTimeMillis() - lastMongoFailureTime > MONGO_RETRY_INTERVAL_MS);
        if (shouldTryMongo) {
            try {
                mongoHealthy = true;
                return recommendationRepository.findAll();
            } catch (Exception e) {
                mongoHealthy = false;
                lastMongoFailureTime = System.currentTimeMillis();
                log.warn("MongoDB query for all recommendations failed: {}", e.getMessage());
            }
        }
        return new ArrayList<>(inMemoryRecommendations.values());
    }

    public Recommendation getRecommendationForActivity(String activityId) {
        // 1. Check in-memory store first for instant response
        Recommendation cached = inMemoryRecommendations.get(activityId);
        if (cached != null) {
            return cached;
        }

        // 2. Try DB if healthy
        boolean shouldTryMongo = mongoHealthy || (System.currentTimeMillis() - lastMongoFailureTime > MONGO_RETRY_INTERVAL_MS);
        if (shouldTryMongo) {
            try {
                Optional<Recommendation> existing = recommendationRepository.findByActivityId(activityId);
                if (existing.isPresent()) {
                    mongoHealthy = true;
                    inMemoryRecommendations.put(activityId, existing.get());
                    return existing.get();
                }
            } catch (Exception e) {
                mongoHealthy = false;
                lastMongoFailureTime = System.currentTimeMillis();
                log.warn("MongoDB read failed for activityId {}: {}", activityId, e.getMessage());
            }
        }

        // 3. Generate on-demand
        log.info("Generating on-demand AI recommendation for activityId: {}", activityId);
        Activity targetActivity = null;
        if (shouldTryMongo) {
            try {
                Optional<Activity> activityOpt = activityRepository.findById(activityId);
                if (activityOpt.isPresent()) {
                    targetActivity = activityOpt.get();
                }
            } catch (Exception e) {
                log.warn("MongoDB lookup failed for activity: {}", e.getMessage());
            }
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

        if (mongoHealthy) {
            try {
                recommendationRepository.save(newRec);
            } catch (Exception e) {
                log.warn("Could not save generated recommendation to MongoDB: {}", e.getMessage());
            }
        }

        return newRec;
    }

}

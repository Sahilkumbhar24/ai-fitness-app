package com.fitness.aiservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.ActivityMongoRepository;
import com.fitness.aiservice.repository.RecommendationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final ActivityMongoRepository activityMongoRepository;
    private final ActivityAIService activityAIService;

    public List<Recommendation> getUserRecommendation(String userId) {
        return recommendationRepository.findByUserId(userId);
    }

    public List<Recommendation> getAllRecommendations() {
        return recommendationRepository.findAll();
    }

    public Recommendation getRecommendationForActivity(String activityId) {
        Optional<Recommendation> existing = recommendationRepository.findByActivityId(activityId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // On-demand generation if Kafka message wasn't used or delayed
        log.info("Recommendation not found in DB for activityId: {}, attempting on-demand AI generation", activityId);
        Optional<Activity> activityOpt = activityMongoRepository.findById(activityId);
        if (activityOpt.isPresent()) {
            Recommendation newRec = activityAIService.generateRecommendation(activityOpt.get());
            return recommendationRepository.save(newRec);
        }

        throw new RuntimeException("Activity not found for activityId: " + activityId);
    }

}

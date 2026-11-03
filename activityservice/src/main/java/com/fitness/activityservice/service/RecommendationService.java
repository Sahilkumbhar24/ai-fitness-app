package com.fitness.activityservice.service;

import java.util.List;
import java.util.Optional;

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

        log.info("Recommendation not found in DB for activityId: {}, attempting on-demand AI generation", activityId);
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isPresent()) {
            Recommendation newRec = activityAIService.generateRecommendation(activityOpt.get());
            return recommendationRepository.save(newRec);
        }

        log.warn("Activity record not yet found for activityId: {}, generating baseline recommendation", activityId);
        Activity placeholder = Activity.builder()
            .id(activityId)
            .duration(30)
            .caloriesBurned(250)
            .build();
        Recommendation newRec = activityAIService.generateRecommendation(placeholder);
        return recommendationRepository.save(newRec);
    }

}

package com.fitness.aiservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.ActivityMongoRepository;
import com.fitness.aiservice.repository.RecommendationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final ActivityAIService activityAIService;
    private final RecommendationRepository recommendationRepository;
    private final ActivityMongoRepository activityMongoRepository;

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "aiservice-group")
    public void processActivity(Activity activity){
        log.info("Recieved Activity for processing: {}", activity.getUserId());
        try {
            activityMongoRepository.save(activity);
        } catch (Exception e) {
            log.warn("Could not save activity to mongo repository: {}", e.getMessage());
        }
        Recommendation recommendation = activityAIService.generateRecommendation(activity);
        recommendationRepository.save(recommendation);
        log.info("Recommendation saved for user: {}", activity.getUserId());
    }
}
